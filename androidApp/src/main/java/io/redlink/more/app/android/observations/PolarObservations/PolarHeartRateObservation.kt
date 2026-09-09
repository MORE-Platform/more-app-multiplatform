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
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.observations.pauseObservation
import io.redlink.more.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.extensions.anyNameIn
import io.redlink.more.observations.Observation
import io.redlink.more.observations.observationTypes.PolarVerityHeartRateType
import io.redlink.more.scopes.Scope
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import io.redlink.more.services.bluetooth.polar.PolarStates
import io.redlink.more.viewModels.bluetoothConnection.PolarController
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

class PolarHeartRateObservation(repos: MainRepository) :
    Observation(
        repos,
        observationType = PolarVerityHeartRateType(permissions)
    ) {
    private val bleManager = BluetoothStateManagement
    private val deviceIdentifier = setOf("Polar")
    private val polarConnector = MoreApplication.polarConnector!!
    private var heartRateJob: Job? = null
    private var deviceConnectionListener: Job? = null

    private val polarController = PolarController(repos)

    init {
        Scope.launch {
            polarController.hrFeatureChange.collect { (studyActive, hrReady) ->
                if (studyActive) {
                    if (hrReady) {
                        MoreApplication.shared!!.observationManager.updateTaskStates()
                    } else {
                        pauseObservation(
                            super.observationType
                        )
                    }
                }
            }
        }
    }

    override fun start(): Boolean {
        Napier.d(tag = "PolarHeartRateObservation::start") { "Trying to start Polar Verity Heart Rate Observation..." }
        if (observerAccessible()) {
            val polarDevices = bleManager.connectedDevices.value.filter {
                (it.deviceName?.lowercase()?.contains("polar") ?: false) && it.address != null
            }
            return polarDevices.firstOrNull()?.let {
                try {
                    // SDK 8 exposes HR streaming as a cold Flow; collecting it starts the stream and
                    // cancelling the job stops it (the RxJava Disposable is gone).
                    heartRateJob = Scope.launch {
                        polarConnector.polarApi.startHrStreaming(it.address!!)
                            .catch { error ->
                                Napier.e(
                                    tag = "PolarHeartRateObservation::start",
                                    message = "HR Recording error: ${error.stackTraceToString()}"
                                )
                                pauseObservation(PolarVerityHeartRateType(emptySet()))
                                showObservationErrorNotification(
                                    stringResource(R.string.observation_bluetooth_error),
                                    stringResource(R.string.observation_error)
                                )
                            }
                            .collect { polarData ->
                                polarData.samples.firstOrNull()?.let { sample ->
                                    storeData(mapOf("hr" to sample.hr))
                                }
                            }
                    }.second
                    deviceConnectionListener = listenToDeviceConnection()
                    true
                } catch (exception: Exception) {
                    Napier.e(tag = "PolarHeartRateObservation::start") { exception.stackTraceToString() }
                    showObservationErrorNotification(
                        stringResource(R.string.observation_cannot_start),
                        stringResource(R.string.observation_error)
                    )
                    false
                }
            } ?: run {
                Napier.d(tag = "PolarHeartRateObservation::start") { "No connected devices..." }
                showObservationErrorNotification(
                    stringResource(R.string.observation_cannot_start),
                    stringResource(R.string.observation_error)
                )
                false
            }
        }
        Napier.d(tag = "PolarHeartRateObservation::start") { "No connected devices..." }
        showObservationErrorNotification(
            stringResource(R.string.observation_cannot_start),
            stringResource(R.string.observation_error)
        )
        return false
    }

    override fun stop(onCompletion: () -> Unit) {
        heartRateJob?.cancel()
        heartRateJob = null
        deviceConnectionListener?.cancel()
        deviceConnectionListener = null
        onCompletion()
    }

    override fun observerErrors(): Set<String> {
        val errors = mutableSetOf<String>()
        if (!hasPermissions(MoreApplication.appContext!!)) {
            errors.add("error_access_bluetooth")
            PolarStates.hrFeatureReady(false)
        }
        if (!BluetoothStateListener.bluetoothEnabled.value) {
            errors.add("bluetooth_disabled")
            PolarStates.hrFeatureReady(false)
        }
        if (!MoreApplication.shared!!.bluetoothController.observerDeviceAccessible(
                deviceIdentifier
            )
        ) {
            PolarStates.hrFeatureReady(false)
            errors.add("device_not_connected")
            errors.add(ERROR_DEVICE_NOT_CONNECTED)
        } else if (!PolarStates.hrFeatureReady.value) {
            errors.add("hr_unavailable")
        }
        return errors
    }

    override fun bleDevicesNeeded(): Set<String> {
        return deviceIdentifier
    }

    override fun ableToAutomaticallyStart(): Boolean {
        return observerAccessible()
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
    }

    private fun hasPermissions(context: Context): Boolean {
        permissions.forEach { permission ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                Napier.e(tag = "PolarHeartRateObservation::hasPermission") { "Polar has no bluetooth permissions!" }
                return false
            }
        }
        Napier.d(tag = "PolarHeartRateObservation::hasPermission") { "Polar has Bluetooth Permission!" }
        return true
    }

    private fun listenToDeviceConnection(): Job {
        return Scope.launch {
            BluetoothStateManagement.connectedDevices.collect { devices ->
                if (!deviceIdentifier.anyNameIn(devices)) {
                    pauseObservation(PolarVerityHeartRateType(emptySet()))
                    PolarStates.hrFeatureReady(false)
                    Napier.d(tag = "PolarHeartRateObservation:::listenToDeviceConnection") { "HR Feature removed!" }
                }
            }
        }.second
    }
}
