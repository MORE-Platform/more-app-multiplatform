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
package io.redlink.umm.blendedcare.app.android.observations.HR

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import io.github.aakira.napier.Napier
import io.reactivex.rxjava3.disposables.Disposable
import io.redlink.umm.blendedcare.app.android.MoreApplication
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.extensions.stringResource
import io.redlink.umm.blendedcare.app.android.observations.pauseObservation
import io.redlink.umm.blendedcare.app.android.observations.showPermissionAlertDialog
import io.redlink.umm.blendedcare.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.extensions.anyNameIn
import io.redlink.umm.participant.observations.Observation
import io.redlink.umm.participant.observations.observationTypes.PolarVerityHeartRateType
import io.redlink.umm.participant.scopes.Scope
import io.redlink.umm.participant.services.bluetooth.BluetoothStateManagement
import io.redlink.umm.participant.services.bluetooth.polar.PolarStates
import io.redlink.umm.participant.viewModels.bluetoothConnection.PolarController
import kotlinx.coroutines.Job

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
    private val polarConnector = MoreApplication.Companion.polarConnector!!
    private var heartRateDisposable: Disposable? = null
    private var deviceConnectionListener: Job? = null

    private val polarController = PolarController(repos)

    init {
        Scope.launch {
            polarController.hrFeatureChange.collect { (studyActive, hrReady) ->
                if (studyActive) {
                    if (hrReady) {
                        MoreApplication.Companion.shared!!.observationManager.updateTaskStates()
                    } else {
                        Observation.pauseObservation(
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
                    heartRateDisposable =
                        polarConnector.polarApi.startHrStreaming(it.address!!)
                            .subscribe(
                                { polarData ->
                                    storeData(mapOf("hr" to polarData.samples[0].hr))
                                },
                                { error ->
                                    Napier.e(
                                        tag = "PolarHeartRateObservation::start",
                                        message = "HR Recording error: ${error.stackTraceToString()}"
                                    )
                                    pauseObservation(PolarVerityHeartRateType(emptySet()))
                                    showObservationErrorNotification(
                                        stringResource(R.string.observation_bluetooth_error),
                                        stringResource(R.string.observation_error)
                                    )
                                })
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
        heartRateDisposable?.dispose()
        deviceConnectionListener?.cancel()
        deviceConnectionListener = null
        onCompletion()
    }

    override fun observerErrors(): Set<String> {
        val errors = mutableSetOf<String>()
        if (!hasPermissions(MoreApplication.Companion.appContext!!)) {
            errors.add("error_access_bluetooth")
            showPermissionAlertDialog()
            PolarStates.hrFeatureReady(false)
        }
        if (!BluetoothStateListener.bluetoothEnabled.value) {
            errors.add("bluetooth_disabled")
            PolarStates.hrFeatureReady(false)
        }
        if (!MoreApplication.Companion.shared!!.bluetoothController.observerDeviceAccessible(
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
                    Napier.d(tag = "PolarHeartRateObservation::Companion::listenToDeviceConnection") { "HR Feature removed!" }
                }
            }
        }.second
    }
}
