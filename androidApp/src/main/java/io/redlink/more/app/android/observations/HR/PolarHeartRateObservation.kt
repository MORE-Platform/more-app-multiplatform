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
package io.redlink.more.app.android.observations.HR

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import io.github.aakira.napier.Napier
import io.reactivex.rxjava3.disposables.Disposable
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.observations.pauseObservation
import io.redlink.more.app.android.observations.showPermissionAlertDialog
import io.redlink.more.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.more.more_app_mutliplatform.extensions.anyNameIn
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.PolarVerityHeartRateType
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDeviceManager
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow

private val permissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        setOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

class PolarHeartRateObservation :
    Observation(observationType = PolarVerityHeartRateType(permissions)) {
    private val deviceManager = BluetoothDeviceManager
    private val deviceIdentifier = setOf("Polar")
    private val polarConnector = MoreApplication.polarConnector!!
    private var heartRateDisposable: Disposable? = null
    private var deviceConnectionListener: Job? = null

    override fun start(): Boolean {
        Napier.d(tag = "PolarHeartRateObservation::start") { "Trying to start Polar Verity Heart Rate Observation..." }
        if (observerAccessible()) {
            val polarDevices = deviceManager.connectedDevices.value.filter {
                (it.deviceName?.lowercase()?.contains("polar") ?: false) && it.address != null
            }
            return polarDevices.firstOrNull()?.let {
                try {
                    heartRateDisposable =
                        polarConnector.polarApi.startHrStreaming(it.address!!).subscribe(
                            { polarData ->
                                storeData(mapOf("hr" to polarData.samples[0].hr))
                            },
                            { error ->
                                Napier.e(
                                    tag = "PolarHeartRateObservation::start",
                                    message = "HR Recording error: ${error.stackTraceToString()}"
                                )
                                pauseObservation(PolarVerityHeartRateType(emptySet()))
                            })
                    deviceConnectionListener = listenToDeviceConnection()
                    true
                } catch (exception: Exception) {
                    Napier.e(tag = "PolarHeartRateObservation::start") { exception.stackTraceToString() }
                    false
                }
            } ?: run {
                Napier.d(tag = "PolarHeartRateObservation::start") { "No connected devices..." }
                false
            }
        }
        Napier.d(tag = "PolarHeartRateObservation::start") { "No connected devices..." }
        showPermissionAlertDialog()
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
        if (hasPermissions(MoreApplication.appContext!!)) {
            errors.add("No permission to access bluetooth!")
        } else if (!BluetoothStateListener.bluetoothEnabled.value) {
            errors.add("Bluetooth disabled!")
        } else if (
            MoreApplication.shared!!.bluetoothController.observerDeviceAccessible(deviceIdentifier)
        ) {
            errors.add("No viable device connected!")
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
            BluetoothDeviceManager.connectedDevices.collect { devices ->
                if (!deviceIdentifier.anyNameIn(devices)) {
                    pauseObservation(PolarVerityHeartRateType(emptySet()))
                    hrReady.set(false)
                    Napier.d(tag = "PolarHeartRateObservation::Companion::listenToDeviceConnection") { "HR Feature removed!" }
                }
            }
        }.second
    }

    companion object {
        val hrReady: MutableStateFlow<Boolean> = MutableStateFlow(false)

        fun setHRReady() {
            if (!hrReady.value) {
                hrReady.set(true)
                MoreApplication.shared!!.observationManager.startObservationType(
                    PolarVerityHeartRateType(
                        emptySet()
                    ).observationType
                )
                Napier.d(tag = "PolarHeartRateObservation::Companion::polarDeviceDisconnected") { "HR Feature Ready!" }
            }
        }
    }
}