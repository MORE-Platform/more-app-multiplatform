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
import com.polar.sdk.api.model.PolarSensorSetting
import com.polar.sdk.api.model.PolarTemperatureData
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
import io.redlink.more.observations.observationTypes.PolarTempType
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

class PolarTempObservation(repos: MainRepository) :
    Observation(repos, observationType = PolarTempType(permissions)) {

    private val deviceIdentifier = setOf("Polar")
    private val polarController get() = MoreApplication.polarController!!
    private var streamingJob: Job? = null
    private var offlineRecordingJob: Job? = null
    private var setupJob: Job? = null
    private var deviceConnectionListener: Job? = null
    private var offlineMode = false

    override fun start(): Boolean {
        Napier.d(tag = "PolarTempObservation::start") { "Starting Polar Temperature (offline=$offlineMode)..." }
        if (!observerAccessible()) {
            showObservationErrorNotification(
                stringResource(R.string.observation_cannot_start),
                stringResource(R.string.observation_error)
            )
            return false
        }

        val device = polarController.findPolarDevice()
        if (device == null) {
            Napier.d(tag = "PolarTempObservation::start") { "No Polar device connected" }
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
                Napier.e(tag = "PolarTempObservation") { "Setup failed: ${error.message}" }
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
                val items = polarController.stopOfflineRecordingAndFetch(OFFLINE_DATA_TYPE)
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
                val items = polarController.stopOfflineRecordingAndFetch(OFFLINE_DATA_TYPE)
                storeRecording(items)
            } catch (e: Exception) {
                Napier.e(tag = "PolarTempObservation") { "Failed to fetch pending offline data: ${e.message}" }
            }
            offlineRecordingJob = polarController.startOfflineRecording(deviceId, OFFLINE_DATA_TYPE)
        }
    }

    /**
     * Device returns the whole recording at once. Persist it straight to the DB in bounded windows
     * (index views, no full copy; see [PolarController.OFFLINE_STORE_CHUNK_SIZE]) so peak heap stays
     * at ~one window and the in-memory upload queue never holds the whole recording.
     */
    private fun storeRecording(items: List<Any>, onCompletion: () -> Unit = {}) {
        storeWindowedDirectly(
            total = items,
            dataKey = DATA_KEY,
            chunkSize = PolarController.OFFLINE_STORE_CHUNK_SIZE,
            transform = { window ->
                processTemperatureSamples(
                    window.filterIsInstance<PolarTemperatureData.PolarTemperatureDataSample>()
                )
            },
            onCompletion = onCompletion,
        )
    }

    private fun startStreaming(deviceId: String) {
        streamingJob = Scope.launch {
            val api = polarController.getPolarApi()
            val settings = try {
                api.requestStreamSettings(deviceId, STREAM_DATA_TYPE)
            } catch (e: Exception) {
                Napier.e(tag = "PolarTempObservation") { "Temp settings request failed: ${e.message}" }
                PolarSensorSetting(
                    hashMapOf(
                        PolarSensorSetting.SettingType.SAMPLE_RATE to 1,
                        PolarSensorSetting.SettingType.RESOLUTION to 1
                    )
                )
            }
            Napier.d(tag = "PolarTempObservation") { "Using temperature settings: ${settings.settings}" }
            api.startTemperatureStreaming(deviceId, settings)
                .catch { error ->
                    Napier.e(tag = "PolarTempObservation") { "Temperature stream failed: ${error.message}" }
                    pauseObservation(PolarTempType(emptySet()))
                    showObservationErrorNotification(
                        stringResource(R.string.observation_bluetooth_error),
                        stringResource(R.string.observation_error)
                    )
                }
                .collect { data ->
                    data.samples.firstOrNull()?.let { sample ->
                        // Live samples are stored one row per sample, flat -- the offline DATA_KEY
                        // wrapper is only for batched recordings.
                        storeData(
                            mapOf(
                                "temp" to sample.temperature,
                                "timestamp" to sample.timeStamp
                            )
                        )
                    }
                }
        }.second
    }

    data class tmpItem(
        val temp: Float,
        val timestamp: Long,
    )

    fun processTemperatureSamples(samples: List<PolarTemperatureData.PolarTemperatureDataSample>?): List<tmpItem> {
        if (samples.isNullOrEmpty()) return emptyList()
        return samples.map { sample ->
            tmpItem(
                temp = sample.temperature,
                timestamp = sample.timeStamp
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
                        pauseObservation(PolarTempType(emptySet()))
                        polarController.onDeviceDisconnected()
                    }
                }
            }
        }.second
    }

    private companion object {
        /** Offline records skin temperature; the live stream uses the plain temperature type. */
        val OFFLINE_DATA_TYPE = PolarBleApi.PolarDeviceDataType.SKIN_TEMPERATURE
        val STREAM_DATA_TYPE = PolarBleApi.PolarDeviceDataType.TEMPERATURE
        const val DATA_KEY = "polar360tempdata"
    }
}
