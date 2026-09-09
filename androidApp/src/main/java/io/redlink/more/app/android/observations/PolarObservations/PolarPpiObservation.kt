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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarPpiData
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.observations.pauseObservation
import io.redlink.more.app.android.observations.showPermissionAlertDialog
import io.redlink.more.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.extensions.anyNameIn
import io.redlink.more.observations.Observation
import io.redlink.more.observations.observationTypes.PolarPpiType
import io.redlink.more.scopes.Scope
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch

private val permissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (Build.VERSION.SDK_INT >= 34) {
            setOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE
            )
        } else {
            setOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    } else {
        setOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

class PolarPpiObservation(repos: MainRepository) :
    Observation(repos, observationType = PolarPpiType(permissions)) {

    private val deviceIdentifier = setOf("Polar")
    private val polarController get() = MoreApplication.polarController!!
    private var streamingJob: Job? = null
    private var offlineRecordingJob: Job? = null
    private var setupJob: Job? = null
    private var deviceConnectionListener: Job? = null
    private var offlineMode = false

    companion object {
        /**
         * Bounds of the offline recording, in Polar nanoseconds. Written by [PolarController] as it
         * stops and lists the recording, read here to pad the sparse samples to 1 Hz.
         */
        var recording_startTimestamp: Long? = null
        var recording_endTimestamp: Long? = null

        /**
         * Pads the sparse offline PPI recording to 1 Hz as a LAZY sequence -- one [ppi_data] per
         * second from the recording start to [endNs], real samples aligned to their slot and gaps
         * filled with corrupted (hr=-99) slots. Nothing is materialised here; the caller streams it
         * straight to the DB via `storeWindowedDirectly` so a multi-day recording never becomes one
         * giant list in memory. Only the sorted raw samples (sparse, already resident) are held.
         */
        fun paddedSequence(
            samples: List<ppi_data>,
            startNs: Long,
            endNs: Long
        ): Sequence<ppi_data> {
            if (endNs <= 0) return samples.asSequence()

            val step = 1_000_000_000L
            val sorted = if (samples.isEmpty()) emptyList() else samples.sortedBy { it.timestamp }

            val rawStart = when {
                sorted.isNotEmpty() ->
                    if (startNs > 0) minOf(startNs, sorted.first().timestamp) else sorted.first().timestamp

                startNs > 0 -> startNs   // no real samples: fill the whole window with corrupted slots
                else -> return samples.asSequence()
            }
            val effectiveStart = (rawStart / step) * step
            if (effectiveStart >= endNs) return samples.asSequence()

            return sequence {
                var sampleIndex = 0
                var cursor = effectiveStart
                while (cursor < endNs) {
                    val slotEnd = cursor + step
                    while (sampleIndex < sorted.size && sorted[sampleIndex].timestamp < cursor) {
                        sampleIndex++
                    }
                    if (sampleIndex < sorted.size && sorted[sampleIndex].timestamp < slotEnd) {
                        val s = sorted[sampleIndex]
                        yield(
                            ppi_data(
                                hr = s.hr,
                                timestamp = cursor,
                                ppiInMs = s.ppiInMs,
                                ppiErrorEstimate = s.ppiErrorEstimate,
                                skinContact = s.skinContact
                            )
                        )
                        sampleIndex++
                        while (sampleIndex < sorted.size && sorted[sampleIndex].timestamp < slotEnd) {
                            sampleIndex++
                        }
                    } else {
                        yield(
                            ppi_data(
                                hr = -99,
                                timestamp = cursor,
                                ppiInMs = 0,
                                ppiErrorEstimate = 65535,
                                skinContact = false
                            )
                        )
                    }
                    cursor = slotEnd
                }
            }
        }
    }

    override fun start(): Boolean {
        Napier.d(tag = "PolarPpiObservation::start") { "Starting Polar PPI (offline=$offlineMode)..." }
        if (!observerAccessible()) {
            showObservationErrorNotification(
                stringResource(R.string.observation_cannot_start),
                stringResource(R.string.observation_error)
            )
            return false
        }

        val device = polarController.findPolarDevice()
        if (device == null) {
            Napier.d(tag = "PolarPpiObservation::start") { "No Polar device connected" }
            showObservationErrorNotification(
                stringResource(R.string.observation_cannot_start),
                stringResource(R.string.observation_error)
            )
            return false
        }

        val deviceId = device.deviceId!!
        setupJob = polarController.ensureReady(deviceId, offlineMode = offlineMode,
            onReady = {
                if (offlineMode) {
                    drainThenRestart(deviceId)
                } else {
                    startStreaming(deviceId)
                }
            },
            onError = { error ->
                Napier.e(tag = "PolarPpiObservation") { "Setup failed: ${error.message}" }
                showObservationErrorNotification(
                    stringResource(R.string.observation_cannot_start),
                    stringResource(R.string.observation_error)
                )
            }
        )

        deviceConnectionListener = listenToDeviceConnection()
        return true
    }

    override fun stop(onCompletion: () -> Unit) {
        setupJob?.cancel()
        setupJob = null
        deviceConnectionListener?.cancel()
        deviceConnectionListener = null
        if (offlineMode) {
            offlineRecordingJob?.cancel()
            offlineRecordingJob = null
            Scope.launch {
                val items = polarController.stopOfflineRecordingAndFetch(DATA_TYPE)
                storeRecording(items, onCompletion)
            }
        } else {
            streamingJob?.cancel()
            streamingJob = null
            onCompletion()
        }
    }

    private fun drainThenRestart(deviceId: String) {
        Scope.launch {
            try {
                val items = polarController.stopOfflineRecordingAndFetch(DATA_TYPE)
                storeRecording(items)
            } catch (e: Exception) {
                Napier.e(tag = "PolarPpiObservation") { "Failed to fetch pending offline data: ${e.message}" }
            }
            offlineRecordingJob = polarController.startOfflineRecording(deviceId, DATA_TYPE)
        }
    }

    /**
     * PPI padding is global (fills 1 Hz slots across the whole recording). Pad LAZILY and stream each
     * bounded window straight to the DB, so the dense padded recording is never materialised whole --
     * OOM-safe for arbitrarily long recordings.
     */
    private fun storeRecording(items: List<Any>, onCompletion: () -> Unit = {}) {
        storeWindowedDirectly(
            total = paddedSequence(
                samples = processPpiSamples(items.filterIsInstance<PolarPpiData.PolarPpiSample>()),
                startNs = recording_startTimestamp ?: 0L,
                endNs = recording_endTimestamp ?: 0L
            ),
            dataKey = DATA_KEY,
            chunkSize = PolarController.OFFLINE_STORE_CHUNK_SIZE,
            transform = { it },
            onCompletion = onCompletion,
        )
    }

    private fun startStreaming(deviceId: String) {
        streamingJob = Scope.launch {
            polarController.getPolarApi().startPpiStreaming(deviceId)
                .catch { error ->
                    Napier.e(tag = "PolarPpiObservation") { "PPI stream failed: ${error.message}" }
                    pauseObservation(PolarPpiType(emptySet()))
                    showObservationErrorNotification(
                        stringResource(R.string.observation_bluetooth_error),
                        stringResource(R.string.observation_error)
                    )
                }
                .collect { data ->
                    // Live samples are stored one row per sample, flat -- the offline DATA_KEY
                    // wrapper is only for batched recordings.
                    processPpiSamples(data.samples).forEach { sample ->
                        storeData(
                            mapOf(
                                "hr" to sample.hr,
                                "timestamp" to sample.timestamp,
                                "ppiInMs" to sample.ppiInMs,
                                "ppiErrorEstimate" to sample.ppiErrorEstimate,
                                "skinContact" to sample.skinContact
                            )
                        )
                    }
                }
        }.second
    }

    data class ppi_data(
        val hr: Int,
        val timestamp: Long,
        val ppiInMs: Int,
        val ppiErrorEstimate: Int,
        var skinContact: Boolean
    )

    fun processPpiSamples(samples: List<PolarPpiData.PolarPpiSample>?): List<ppi_data> {
        if (samples.isNullOrEmpty()) return emptyList()
        return samples.map {
            ppi_data(
                hr = it.hr,
                timestamp = it.timeStamp.toLong(),
                ppiInMs = it.ppi,
                ppiErrorEstimate = it.errorEstimate,
                skinContact = it.skinContactStatus
            )
        }
    }

    override fun observerErrors(): Set<String> {
        val errors = mutableSetOf<String>()
        if (!hasPermissions(MoreApplication.appContext!!)) {
            errors.add("error_access_bluetooth")
            showPermissionAlertDialog()
        }
        if (!BluetoothStateListener.bluetoothEnabled.value) {
            errors.add("bluetooth_disabled")
        }
        if (offlineMode) {
            if (polarController.findPolarDevice() == null) {
                errors.add("device_not_connected")
                errors.add(ERROR_DEVICE_NOT_CONNECTED)
            }
        } else {
            if (!MoreApplication.shared!!.bluetoothController.observerDeviceAccessible(deviceIdentifier)) {
                errors.add("device_not_connected")
                errors.add(ERROR_DEVICE_NOT_CONNECTED)
            }
        }
        return errors
    }

    override fun shouldAutoPause(): Boolean = !offlineMode

    override fun bleDevicesNeeded(): Set<String> = deviceIdentifier

    override fun ableToAutomaticallyStart(): Boolean = observerAccessible()

    override fun applyObservationConfig(settings: Map<String, Any>) {
        offlineMode = polarController.isOfflineRecordingMode(settings)
    }

    private fun hasPermissions(context: Context): Boolean {
        return permissions.all {
            ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_DENIED
        }
    }

    private fun listenToDeviceConnection(): Job {
        return Scope.launch {
            BluetoothStateManagement.connectedDevices.collect { devices ->
                if (!deviceIdentifier.anyNameIn(devices)) {
                    if (offlineMode) {
                        updateObservationErrors()
                    } else {
                        pauseObservation(PolarPpiType(emptySet()))
                        polarController.onDeviceDisconnected()
                    }
                }
            }
        }.second
    }
}

private val DATA_TYPE = PolarBleApi.PolarDeviceDataType.PPI
private const val DATA_KEY = "polar360ppidata"
