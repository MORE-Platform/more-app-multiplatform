/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.observations.PolarObservations

import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarFirstTimeUseConfig
import com.polar.sdk.api.model.PolarOfflineRecordingData
import com.polar.sdk.api.model.PolarOfflineRecordingEntry
import com.polar.sdk.api.model.PolarSensorSetting
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.scopes.Scope
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Drives Polar offline recording and device setup for every Polar observation (ACC, HR, PPI, TEMP).
 *
 * Polar SDK 8 replaced the RxJava surface with coroutines: every call here is `suspend` or collects
 * a `Flow`. The old implementation serialised BLE work through a single-threaded scheduler plus a
 * `concatMapSingle` operation queue; [bleMutex] is the direct coroutine equivalent.
 *
 * Storing is not this class's job. Callers get the raw SDK samples back and persist them
 * themselves, in bounded windows, via `Observation.storeWindowedDirectly`.
 */
class PolarController {

    private val polarConnector get() = MoreApplication.polarConnector!!
    private val bleManager = BluetoothStateManagement

    private var sdkModeEnabled = false
    private var currentDeviceId: String? = null

    /**
     * A Polar device serves one request at a time, and several observations (HR, ACC, PPI, TEMP)
     * share the same device. Every multi-step operation holds this for its whole sequence, so a
     * stop/drain/start cycle cannot interleave with another observation's.
     *
     * The lock is not reentrant: public entry points take it, `*Locked` helpers assume it is held.
     */
    private val bleMutex = Mutex()

    fun findPolarDevice(polarModel: String = POLAR_360_MODEL): BluetoothDeviceEntity? {
        return bleManager.connectedDevices.value.firstOrNull {
            val name = it.deviceName?.lowercase() ?: return@firstOrNull false
            name.contains("polar") && name.contains(polarModel) && it.address != null
        }
    }

    fun getCurrentDeviceId(): String? = currentDeviceId

    fun getPolarApi() = polarConnector.polarApi

    fun isOfflineRecordingMode(config: Map<String, Any>): Boolean {
        Napier.d(tag = "PolarController::isOfflineRecordingMode") { config.toString() }
        return config[CONFIG_OFFLINE_RECORDING] == true ||
            config[CONFIG_OFFLINE_RECORDING]?.toString()?.lowercase() == "true"
    }

    fun onDeviceDisconnected() {
        sdkModeEnabled = false
        currentDeviceId = null
    }

    /**
     * Runs first-time-use setup (if the device still needs it) and syncs the device clock, then
     * reports back on the caller's side via [onReady] / [onError].
     *
     * Callback-shaped rather than `suspend` because observations are started from
     * `Observation.start(): Boolean`, which cannot suspend.
     */
    fun ensureReady(
        deviceId: String,
        offlineMode: Boolean,
        onReady: () -> Unit,
        onError: (Throwable) -> Unit
    ): Job {
        currentDeviceId = deviceId
        return Scope.launch {
            try {
                val setupDone = bleMutex.withLock {
                    val done = checkIfDeviceIsSetupLocked(deviceId)
                    syncDeviceTimeLocked(deviceId)
                    // Offline recording of PPI/HR requires SDK mode to be OFF, for both modes.
                    disableSdkModeLocked(deviceId)
                    done
                }
                if (setupDone) {
                    onReady()
                } else {
                    Napier.e(tag = "PolarController::ensureReady") { "Polar FTU failed for $deviceId" }
                    onError(RuntimeException("First time use setup failed"))
                }
            } catch (e: Exception) {
                Napier.e(tag = "PolarController::ensureReady") { "Setup check failed: ${e.stackTraceToString()}" }
                onError(e)
            }
        }.second
    }

    /**
     * Starts an offline recording for [dataType], clearing any recording still running for it first.
     * Fire-and-forget: returns the [Job] doing the work so the caller can cancel it.
     */
    fun startOfflineRecording(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType,
        settings: PolarSensorSetting? = null
    ): Job {
        Napier.d(tag = "PolarController::startOfflineRecording") { "[$dataType] Starting offline recording on device=$deviceId" }
        return Scope.launch {
            bleMutex.withLock {
                disableSdkModeLocked(deviceId)
                // A recording still running would keep its file open; stop it before starting fresh.
                try {
                    polarConnector.polarApi.stopOfflineRecording(deviceId, dataType)
                } catch (e: Exception) {
                    Napier.w(tag = "PolarController::startOfflineRecording") { "[$dataType] pre-stop error (ignored): ${e.message}" }
                }
                try {
                    resolveAndStartOfflineRecordingLocked(deviceId, dataType, settings)
                    Napier.d(tag = "PolarController::startOfflineRecording") { "[$dataType] Offline recording started successfully" }
                } catch (e: Exception) {
                    Napier.e(tag = "PolarController::startOfflineRecording") { "[$dataType] Failed to start: ${e.stackTraceToString()}" }
                }
            }
        }.second
    }

    /**
     * Stops the running recording for [dataType] and reads every recording of that type off the
     * device, returning the raw SDK samples and deleting the entries.
     *
     * Returns an empty list when no device is known or nothing could be read. Callers persist the
     * result in bounded windows -- never log or copy it whole, a long recording holds millions of
     * samples.
     */
    suspend fun stopOfflineRecordingAndFetch(
        dataType: PolarBleApi.PolarDeviceDataType
    ): List<Any> {
        val deviceId = currentDeviceId ?: run {
            Napier.w(tag = "PolarController::stopOfflineRecordingAndFetch") { "[$dataType] currentDeviceId is null -- returning empty" }
            return emptyList()
        }
        return bleMutex.withLock {
            if (dataType == PolarBleApi.PolarDeviceDataType.PPI) {
                val endNs = (System.currentTimeMillis() - POLAR_EPOCH_OFFSET_MILLIS) * 1_000_000L
                PolarPpiObservation.recording_endTimestamp = endNs
                Napier.d(tag = "PolarController::stopOfflineRecordingAndFetch") { "[ppi] recording_endTimestamp=$endNs" }
            }
            Napier.d(tag = "PolarController::stopOfflineRecordingAndFetch") { "[$dataType] Stopping recording on device=$deviceId" }
            try {
                polarConnector.polarApi.stopOfflineRecording(deviceId, dataType)
            } catch (e: Exception) {
                Napier.w(tag = "PolarController::stopOfflineRecordingAndFetch") { "[$dataType] stopOfflineRecording API error (ignored): ${e.message}" }
            }
            drainLocked(deviceId, dataType)
        }
    }

    /** Deletes offline recordings of [dataType] without reading them. Discards unsent data. */
    suspend fun discardOfflineRecordings(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType
    ): Int = bleMutex.withLock {
        var removed = 0
        try {
            entriesOf(deviceId, dataType).forEach {
                polarConnector.polarApi.removeOfflineRecord(deviceId, it)
                removed++
            }
            Napier.w(tag = "PolarController::discardOfflineRecordings") { "Discarded $removed $dataType recording(s)." }
        } catch (e: Exception) {
            Napier.e(tag = "PolarController::discardOfflineRecordings") { e.stackTraceToString() }
        }
        removed
    }

    suspend fun isOfflineRecordingActive(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType
    ): Boolean = bleMutex.withLock {
        try {
            polarConnector.polarApi.getOfflineRecordingStatus(deviceId).contains(dataType)
        } catch (e: Exception) {
            Napier.e(tag = "PolarController::isOfflineRecordingActive") { e.stackTraceToString() }
            false
        }
    }

    // --- operations below assume bleMutex is held ------------------------------------------------

    /**
     * Reads every recording of [dataType] off the device and returns the concatenated raw samples.
     *
     * An entry is deleted once it has been read, so the device does not accumulate recordings. If
     * reading fails the entry is deleted too -- a corrupt recording would otherwise be retried
     * forever. If deletion fails the samples are still returned, so nothing already read is lost.
     */
    private suspend fun drainLocked(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType
    ): List<Any> {
        val entries = try {
            entriesOf(deviceId, dataType)
        } catch (e: Exception) {
            Napier.e(tag = "PolarController::drain") { "[$dataType] listOfflineRecordings error: ${e.message}" }
            return emptyList()
        }
        if (entries.isEmpty()) {
            Napier.d(tag = "PolarController::drain") { "[$dataType] No recordings on $deviceId." }
            return emptyList()
        }
        Napier.d(tag = "PolarController::drain") { "[$dataType] ${entries.size} recording(s) to drain." }

        val all = mutableListOf<Any>()
        for (entry in entries) {
            if (dataType == PolarBleApi.PolarDeviceDataType.PPI) {
                val startNs =
                    (entry.date.toInstant(ZoneOffset.UTC).toEpochMilli() - POLAR_EPOCH_OFFSET_MILLIS) * 1_000_000L
                PolarPpiObservation.recording_startTimestamp = startNs
                Napier.d(tag = "PolarController::drain") { "[ppi] recording_startTimestamp=$startNs (entry.date=${entry.date})" }
            }
            val samples = try {
                polarConnector.polarApi.getOfflineRecord(deviceId, entry, null).extractSamples(dataType, entry)
            } catch (e: Exception) {
                // Unreadable/corrupt entry: remove it so it does not accumulate on the device
                // forever, then move on with no samples from it.
                Napier.w(tag = "PolarController::drain") { "[$dataType] Unreadable entry (date=${entry.date}, size=${entry.size}): ${e.message} -- removing to prevent accumulation" }
                removeQuietly(deviceId, entry)
                continue
            }
            // NOTE: never log the whole sample list -- for a long offline recording that stringifies
            // millions of samples into one giant String and can OOM-kill the app. Log counts only.
            Napier.d(tag = "PolarController::drain") { "[$dataType] extracted ${samples.size} samples, removing entry..." }
            removeQuietly(deviceId, entry)
            all.addAll(samples)
        }
        Napier.d(tag = "PolarController::drain") { "[$dataType] Total samples returned: ${all.size}" }
        return all
    }

    private suspend fun removeQuietly(deviceId: String, entry: PolarOfflineRecordingEntry) {
        try {
            polarConnector.polarApi.removeOfflineRecord(deviceId, entry)
        } catch (e: Exception) {
            // Never lose samples we already read: keep going even if removal failed.
            Napier.e(tag = "PolarController::drain") { "removeOfflineRecord error (ignored): ${e.message}" }
        }
    }

    private fun PolarOfflineRecordingData.extractSamples(
        dataType: PolarBleApi.PolarDeviceDataType,
        entry: PolarOfflineRecordingEntry
    ): List<Any> = when (this) {
        is PolarOfflineRecordingData.PpiOfflineRecording -> data.samples.also {
            if (it.isEmpty()) {
                Napier.w(tag = "PolarController::drain") {
                    "[$dataType] SDK returned 0 samples for entry size=${entry.size} -- likely no skin contact during recording; removing entry"
                }
            }
        }

        is PolarOfflineRecordingData.AccOfflineRecording -> data.samples
        is PolarOfflineRecordingData.HrOfflineRecording -> data.samples
        is PolarOfflineRecordingData.TemperatureOfflineRecording -> data.samples
        is PolarOfflineRecordingData.SkinTemperatureOfflineRecording -> data.samples
        is PolarOfflineRecordingData.PpgOfflineRecording -> data.samples
        else -> emptyList<Any>().also {
            Napier.w(tag = "PolarController::drain") {
                "[$dataType] Unhandled/empty data type: ${this::class.simpleName}, entry size=${entry.size} -- removing entry"
            }
        }
    }

    private suspend fun resolveAndStartOfflineRecordingLocked(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType,
        settings: PolarSensorSetting?
    ) {
        val api = polarConnector.polarApi
        if (dataType == PolarBleApi.PolarDeviceDataType.HR || dataType == PolarBleApi.PolarDeviceDataType.PPI) {
            api.startOfflineRecording(deviceId, dataType)
            return
        }
        try {
            val chosen = settings ?: run {
                // ACC/TEMPERATURE: prefer the device's requested settings (known-good on current
                // firmware).
                val resolved = api.requestOfflineRecordingSettings(deviceId, dataType)
                // Passing the queried settings as-is lets the SDK resolve to the device's max sample
                // rate (4 Hz for skin temperature). Temperature must be recorded at 1 Hz, so pin
                // SAMPLE_RATE to 1 while keeping the device's native resolution/channels.
                if (isTemperature(dataType)) pinTemperatureSampleRate(resolved) else resolved
            }
            Napier.d(tag = "PolarController::$dataType") { "Using settings: ${chosen.settings}" }
            api.startOfflineRecording(deviceId, dataType, chosen, null)
        } catch (settingsError: Exception) {
            // Some newer firmware fails settings negotiation (notably for temperature) -- fall back
            // to a settings-less start.
            Napier.w(tag = "PolarController::$dataType") {
                "Settings-based start failed (${settingsError.message}); retrying without settings"
            }
            api.startOfflineRecording(deviceId, dataType)
        }
    }

    private fun isTemperature(dataType: PolarBleApi.PolarDeviceDataType): Boolean =
        dataType == PolarBleApi.PolarDeviceDataType.TEMPERATURE ||
            dataType == PolarBleApi.PolarDeviceDataType.SKIN_TEMPERATURE

    /**
     * Builds a concrete setting from the device's available offline settings, forcing the sample
     * rate to 1 Hz (the documented native rate for skin temperature). Every other setting type keeps
     * the device's max available value so resolution/channels stay valid.
     */
    private fun pinTemperatureSampleRate(available: PolarSensorSetting): PolarSensorSetting {
        val concrete = available.settings.mapValues { (type, values) ->
            if (type == PolarSensorSetting.SettingType.SAMPLE_RATE) {
                if (values.contains(1)) 1 else (values.minOrNull() ?: 1)
            } else {
                values.maxOrNull() ?: 0
            }
        }
        return PolarSensorSetting(HashMap(concrete))
    }

    private suspend fun entriesOf(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType
    ): List<PolarOfflineRecordingEntry> =
        polarConnector.polarApi.listOfflineRecordings(deviceId)
            .filter { it.type == dataType }
            .toList()

    private suspend fun syncDeviceTimeLocked(deviceId: String) {
        try {
            polarConnector.polarApi.setLocalTime(deviceId, LocalDateTime.now(ZoneOffset.UTC))
            Napier.i(tag = "PolarController::syncDeviceTime") { "Device time synced for $deviceId" }
        } catch (e: Exception) {
            Napier.w(tag = "PolarController::syncDeviceTime") { "Failed to sync device time (ignored): ${e.message}" }
        }
    }

    /** Returns true when the device is (now) set up for use. Runs FTU if it has not been done. */
    private suspend fun checkIfDeviceIsSetupLocked(deviceId: String): Boolean {
        if (polarConnector.polarApi.isFtuDone(deviceId)) return true

        val deviceTime = LocalDateTime.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))

        val profile = PolarUserProfile.load()
        val birthDate = profile?.birthDate ?: LocalDate.now(ZoneId.systemDefault()).minusYears(30)
        val age = profile?.age ?: 30
        val maxHR = (220 - age).coerceIn(120, 220)

        val ftuConfig = PolarFirstTimeUseConfig(
            gender = profile?.gender?.polarGender ?: PolarFirstTimeUseConfig.Gender.FEMALE,
            birthDate = birthDate,
            height = (profile?.heightCm ?: 170).toFloat(),
            weight = (profile?.weightKg ?: 70).toFloat(),
            maxHeartRate = maxHR,
            vo2Max = 35,
            restingHeartRate = 60,
            trainingBackground = 30,
            deviceTime = deviceTime,
            typicalDay = PolarFirstTimeUseConfig.TypicalDay.MOSTLY_SITTING,
            sleepGoalMinutes = 480
        )
        return try {
            polarConnector.polarApi.doFirstTimeUse(deviceId, ftuConfig)
            true
        } catch (e: Exception) {
            Napier.e(tag = "PolarController::checkIfDeviceIsSetup") { "doFirstTimeUse failed: ${e.message}" }
            false
        }
    }

    private suspend fun disableSdkModeLocked(deviceId: String) {
        if (!sdkModeEnabled) return
        try {
            polarConnector.polarApi.disableSDKMode(deviceId)
            sdkModeEnabled = false
            Napier.d(tag = "PolarController") { "SDK mode disabled for $deviceId" }
        } catch (e: Exception) {
            Napier.e(tag = "PolarController") { "Failed to disable SDK mode: ${e.message}" }
        }
    }

    companion object {
        const val CONFIG_OFFLINE_RECORDING = "Offline_recording"

        /**
         * Max samples stored per observation_data row when persisting an extracted offline
         * recording. A whole recording can hold millions of samples; serialising them into one row
         * and later parsing/uploading it OOM-kills the app. Storing in bounded chunks keeps every
         * step (JSON string, DB row, upload payload) small.
         */
        const val OFFLINE_STORE_CHUNK_SIZE = 1000

        const val POLAR_360_MODEL = "360"

        /** Polar timestamps count from 2000-01-01T00:00:00Z. */
        private const val POLAR_EPOCH_OFFSET_MILLIS = 946_684_800_000L
    }
}
