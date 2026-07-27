package io.redlink.more.app.android.observations.Polar

import android.content.ContentValues.TAG
import android.util.Log
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarFirstTimeUseConfig
import com.polar.sdk.api.model.PolarSensorSetting
import com.polar.sdk.api.model.PolarOfflineRecordingData
import io.github.aakira.napier.Napier
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object Polar360Controller {
    const val CONFIG_OFFLINE_RECORDING = "Offline_recording"




    private val polarConnector get() = MoreApplication.polarConnector!!
    private val bleManager = BluetoothStateManagement
    private var ftuDisposable: Disposable? = null
    private var sdkModeEnabled = false
    private var currentDeviceId: String? = null
    private val bleScheduler = Schedulers.single()
    private val operationQueue = PublishSubject.create<Single<List<Any>>>().toSerialized()

    init {
        operationQueue
            .doOnNext { Napier.d(tag = "Polar360Controller::queue") { "Task enqueued, processing next..." } }
            .concatMapSingle { it }
            .subscribe(
                { result -> Napier.d(tag = "Polar360Controller::queue") { "Task completed with ${result.size} items" } },
                { error -> Napier.e(tag = "Polar360Controller::queue") { "BLE operation queue error: ${error.message}" } }
            )
    }




    fun findPolar360Device(): BluetoothDeviceEntity? {
        return bleManager.connectedDevices.value.firstOrNull {
            val name = it.deviceName?.lowercase() ?: return@firstOrNull false
            name.contains("polar") && name.contains("360") && it.address != null
        }
    }

    fun ensureReady(deviceId: String, offlineMode: Boolean, onReady: () -> Unit, onError: (Throwable) -> Unit) {
        currentDeviceId = deviceId
        ftuDisposable?.dispose()
        ftuDisposable = checkIfDeviceIsSetup(deviceId)
            .flatMap { setupDone ->
                syncDeviceTime(deviceId).toSingleDefault(setupDone).onErrorReturnItem(setupDone)
            }
            .subscribeOn(bleScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ setupDone ->
                if (setupDone) {
                    // For any syteaming of offline recording with ppi/hr sdk mode must be off
                    disableSdkMode(deviceId)
                    /*
                    if (offlineMode) {
                        disableSdkMode(deviceId)
                    } else {
                        enableSdkMode(deviceId)
                    }*/
                    onReady()
                } else {
                    Log.e(TAG, "Polar 360 FTU failed")
                    onError(RuntimeException("First time use setup failed"))
                }
            }, { error ->
                Log.e(TAG, "Polar 360 setup check failed: ${error.localizedMessage}")
                onError(error)
            })
    }

    fun startOfflineRecording(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType,
        settings: PolarSensorSetting? = null
    ): Disposable {
        Napier.d(tag = "Polar360Controller::startOfflineRecording") { "[$dataType] Starting offline recording on device=$deviceId" }
        return polarConnector.polarApi.disableSDKMode(deviceId)
            .doOnError { e -> Napier.w(tag = "Polar360Controller::startOfflineRecording") { "[$dataType] disableSDKMode error (ignored): ${e.message}" } }
            .onErrorComplete()
            .doOnComplete { Napier.d(tag = "Polar360Controller::startOfflineRecording") { "[$dataType] SDK mode disabled, stopping any existing recording..." } }
            .andThen(
                polarConnector.polarApi.stopOfflineRecording(deviceId, dataType)
                    .doOnError { e -> Napier.w(tag = "Polar360Controller::startOfflineRecording") { "[$dataType] pre-stop error (ignored): ${e.message}" } }
                    .onErrorComplete()
            )
            .andThen(resolveAndStartOfflineRecording(deviceId, dataType, settings))
            .subscribeOn(bleScheduler)
            .observeOn(bleScheduler)
            .subscribe(
                { Napier.d(tag = "Polar360Controller::startOfflineRecording") { "[$dataType] Offline recording started successfully" } },
                { error -> Napier.e(tag = "Polar360Controller::startOfflineRecording") { "[$dataType] Failed to start: ${error.message}" } }
            )
    }

    private fun resolveAndStartOfflineRecording(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType,
        settings: PolarSensorSetting?
    ): Completable {
        if (dataType == PolarBleApi.PolarDeviceDataType.HR || dataType == PolarBleApi.PolarDeviceDataType.PPI) {
            return polarConnector.polarApi.startOfflineRecording(deviceId, dataType)
        }
        if (settings != null) {
            return polarConnector.polarApi.startOfflineRecording(deviceId, dataType, settings, null)
                .onErrorResumeNext { settingsError ->
                    Napier.w(tag = "Polar360Controller::$dataType") {
                        "Settings-based start failed (${settingsError.message}); retrying without settings"
                    }
                    polarConnector.polarApi.startOfflineRecording(deviceId, dataType)
                }
        }
        // ACC/TEMPERATURE: prefer the device's requested settings (known-good on current
        // firmware). Some newer firmware fails settings negotiation for temperature — if the
        // settings-based start errors, fall back to a settings-less start.
        return polarConnector.polarApi.requestOfflineRecordingSettings(deviceId, dataType)
            .flatMapCompletable { resolvedSettings ->
                // Passing the queried settings as-is lets the SDK resolve to the device's max
                // sample rate (4 Hz for skin temperature). Temperature must be recorded at 1 Hz,
                // so pin SAMPLE_RATE to 1 while keeping the device's native resolution/channels.
                val chosenSettings = if (isTemperature(dataType)) {
                    pinTemperatureSampleRate(resolvedSettings)
                } else {
                    resolvedSettings
                }
                Napier.d(tag = "Polar360Controller::$dataType") { "Using settings: ${chosenSettings.settings}" }
                polarConnector.polarApi.startOfflineRecording(deviceId, dataType, chosenSettings, null)
            }
            .onErrorResumeNext { settingsError ->
                Napier.w(tag = "Polar360Controller::$dataType") {
                    "Settings-based start failed (${settingsError.message}); retrying without settings"
                }
                polarConnector.polarApi.startOfflineRecording(deviceId, dataType)
            }
    }

    private fun isTemperature(dataType: PolarBleApi.PolarDeviceDataType): Boolean =
        dataType == PolarBleApi.PolarDeviceDataType.TEMPERATURE ||
            dataType == PolarBleApi.PolarDeviceDataType.SKIN_TEMPERATURE

    // Builds a concrete setting from the device's available offline settings, forcing the sample
    // rate to 1 Hz (the documented native rate for skin temperature). Every other setting type
    // keeps the device's max available value so resolution/channels stay valid.
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

    fun stopOfflineRecording(dataType: PolarBleApi.PolarDeviceDataType): Single<List<Any>> {
        val deviceId = currentDeviceId ?: run {
            Napier.w(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] currentDeviceId is null — returning empty" }
            return Single.just(emptyList())
        }
        Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] Enqueueing stop+fetch for device=$deviceId" }
        return Single.create { emitter ->
            val task = doStopOfflineRecording(deviceId, dataType)
                .doOnSuccess { emitter.onSuccess(it) }
                .doOnError { emitter.onError(it) }
                .onErrorReturnItem(emptyList())
            operationQueue.onNext(task)
        }
    }

    private fun doStopOfflineRecording(deviceId: String, dataType: PolarBleApi.PolarDeviceDataType): Single<List<Any>> {
        if (dataType == PolarBleApi.PolarDeviceDataType.PPI) {
            val epoch2000Ms = 946_684_800_000L
            val endNs = (System.currentTimeMillis() - epoch2000Ms) * 1_000_000L
            Polar360PpiObservation.recroding_endTimestamp = endNs
            Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[ppi] recroding_endTimestamp=$endNs" }
        }
        Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] Stopping recording on device=$deviceId" }
        return polarConnector.polarApi.stopOfflineRecording(deviceId, dataType)
            .doOnComplete { Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] Recording stopped, listing all recordings..." } }
            .doOnError { e -> Napier.w(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] stopOfflineRecording API error (ignored): ${e.message}" } }
            .onErrorComplete()
            .andThen(
                polarConnector.polarApi.listOfflineRecordings(deviceId)
                    .doOnNext { entry ->
                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") {
                            "[$dataType] Listed entry: type=${entry.type}, date=${entry.date}, size=${entry.size} — matches=${entry.type == dataType}"
                        }
                    }
                    .doOnComplete { Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] listOfflineRecordings completed" } }
                    .doOnError { e -> Napier.e(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] listOfflineRecordings error: ${e.message}" } }
                    .filter { entry -> entry.type == dataType }
                    .concatMap { entry ->
                        if (dataType == PolarBleApi.PolarDeviceDataType.PPI) {
                            val epoch2000Ms = 946_684_800_000L
                            val startNs = (entry.date.toInstant(ZoneOffset.UTC).toEpochMilli() - epoch2000Ms) * 1_000_000L
                            Polar360PpiObservation.recording_startTimestamp = startNs
                            Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[ppi] recording_startTimestamp=$startNs (entry.date=${entry.date})" }
                        }
                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] Fetching record: date=${entry.date}, size=${entry.size}" }
                        polarConnector.polarApi.getOfflineRecord(deviceId, entry, null)
                            .doOnError { e -> Napier.e(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] getOfflineRecord error: ${e.message}" } }
                            .flatMap { data ->
                                val samples: List<Any> = when (data) {
                                    is PolarOfflineRecordingData.PpiOfflineRecording -> data.data.samples.also {

                                        if (it.isEmpty()) {
                                            Napier.w(tag = "Polar360Controller::stopOfflineRecording") {
                                                "[$dataType] SDK returned 0 samples for entry size=${entry.size} — likely no skin contact during recording; removing entry"
                                            }
                                        } else {
                                            Napier.d(tag = "Polar360Controller::stopOfflineRecording") {
                                                "[$dataType] PPI record: ${it.size} samples, firstTimestamp=${it.firstOrNull()?.timeStamp}"
                                            }
                                        }
                                    }
                                    is PolarOfflineRecordingData.AccOfflineRecording -> data.data.samples.also {
                                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") {
                                            "[$dataType] ACC record: ${it.size} samples, firstTimestamp=${it.firstOrNull()?.timeStamp}"
                                        }
                                    }
                                    is PolarOfflineRecordingData.TemperatureOfflineRecording -> data.data.samples.also {
                                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") {
                                            "[$dataType] TEMP record: ${it.size} samples, firstTimestamp=${it.firstOrNull()?.timeStamp}"
                                        }
                                    }
                                    is PolarOfflineRecordingData.SkinTemperatureOfflineRecording -> data.data.samples.also {
                                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") {
                                            "[$dataType] SKIN TEMP record: ${it.size} samples, firstTimestamp=${it.firstOrNull()?.timeStamp}"
                                        }
                                    }
                                    is PolarOfflineRecordingData.PpgOfflineRecording -> data.data.samples.also {
                                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") {
                                            "[$dataType] PPG record: ${it.size} samples"
                                        }
                                    }
                                    else -> emptyList<Any>().also {
                                        Napier.w(tag = "Polar360Controller::stopOfflineRecording") {
                                            "[$dataType] Unhandled/empty data type: ${data::class.simpleName}, entry size=${entry.size} — removing entry"
                                        }
                                    }
                                }
                                Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] extractSamples result: count=${samples.size}, raw=$samples" }
                                Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] Removing entry from device..." }
                                polarConnector.polarApi.removeOfflineRecord(deviceId, entry)
                                    .doOnComplete { Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] Entry removed" } }
                                    .doOnError { e -> Napier.e(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] removeOfflineRecord error: ${e.message}" } }
                                    .andThen(Single.just(samples))
                            }
                            .onErrorResumeNext { e: Throwable ->
                                Napier.w(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] Skipping unreadable entry (date=${entry.date}, size=${entry.size}): ${e.message}" }
                                Single.just(emptyList())
                            }
                            .toFlowable()
                    }
                    .toList()
                    .map { it.flatten() }
                    .doOnSuccess { all -> Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "[$dataType] Total samples returned: ${all.size}" } }
            )
            .subscribeOn(bleScheduler)
            .observeOn(bleScheduler)
    }

    fun isOfflineRecordingMode(config: Map<String, Any>): Boolean {
        Napier.d(tag =  "Polar360Controller:isOfflineRecordingMode"){config.toString()}
        return config[CONFIG_OFFLINE_RECORDING] == true
                || config[CONFIG_OFFLINE_RECORDING]?.toString()?.lowercase() == "true"
    }

    fun getCurrentDeviceId(): String? = currentDeviceId

    private fun enableSdkMode(deviceId: String) {
        if (!sdkModeEnabled) {
            try {
                polarConnector.polarApi.enableSDKMode(deviceId)
                sdkModeEnabled = true
                Napier.d(tag = "Polar360Controller") { "SDK mode enabled for $deviceId" }
            } catch (e: Exception) {
                Napier.e(tag = "Polar360Controller") { "Failed to enable SDK mode: ${e.message}" }
            }
        }
    }

    private fun disableSdkMode(deviceId: String) {
        if (sdkModeEnabled) {
            try {
                polarConnector.polarApi.disableSDKMode(deviceId)
                sdkModeEnabled = false
                Napier.d(tag = "Polar360Controller") { "SDK mode disabled for $deviceId" }
            } catch (e: Exception) {
                Napier.e(tag = "Polar360Controller") { "Failed to disable SDK mode: ${e.message}" }
            }
        }
    }

    fun onDeviceDisconnected() {
        sdkModeEnabled = false
        currentDeviceId = null
    }

    fun getPolarApi() = polarConnector.polarApi

    private fun syncDeviceTime(deviceId: String): Completable {
        val dateTime = LocalDateTime.now(ZoneOffset.UTC)
        return polarConnector.polarApi.setLocalTime(deviceId, dateTime)
            .doOnComplete { Log.i(TAG, "Polar360Controller: Device time synced for $deviceId") }
            .onErrorComplete { error ->
                Log.w(TAG, "Polar360Controller: Failed to sync device time (ignored): ${error.localizedMessage}")
                true
            }
    }

    private fun checkIfDeviceIsSetup(deviceId: String): Single<Boolean> {
        return polarConnector.polarApi.isFtuDone(deviceId)
            .flatMap { ftuDone ->
                if (ftuDone) {
                    Single.just(true)
                } else {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    val deviceTime = sdf.format(calendar.time)

                    val profile = Polar360UserProfile.load()
                    val birthDate = (profile?.birthDate ?: Calendar.getInstance().apply {
                        add(Calendar.YEAR, -30)
                    }.time).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
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

                    polarConnector.polarApi.doFirstTimeUse(deviceId, ftuConfig)
                        .toSingleDefault(true)
                        .onErrorReturnItem(false)
                }
            }
    }
}
