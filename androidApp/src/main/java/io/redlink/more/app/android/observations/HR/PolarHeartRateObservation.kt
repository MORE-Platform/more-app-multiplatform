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

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.reactivex.rxjava3.disposables.Disposable
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.observations.showPermissionAlertDialog
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.PolarVerityHeartRateType
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

private val permissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        setOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )
    }

class PolarHeartRateObservation :
    Observation(observationType = PolarVerityHeartRateType(permissions)) {
    private val deviceIdentifier = setOf("Polar")
    private val polarConnector = MoreApplication.polarConnector!!
    private var heartRateDisposable: Disposable? = null

    override fun start(): Boolean {
        Napier.d(tag = "PolarHeartRateObservation::start") { "Trying to start Polar Verity Heart Rate Observation..." }
        if (observerAccessible()) {
            val polarDevices = MoreApplication.shared!!.mainBluetoothConnector.connected.filter {
                it.deviceName?.lowercase()?.contains("polar") ?: false
            }
            return polarDevices.firstOrNull()?.let {
                try {
                    if (it.address != null) {
                        heartRateDisposable = polarConnector.polarApi.startHrStreaming(it.address!!).subscribe(
                            { polarData ->
                                storeData(mapOf("hr" to polarData.samples[0].hr))
                            },
                            { error ->
                                Napier.e(tag = "PolarHeartRateObservation::start", message = "HR Recording error: ${error.stackTraceToString()}")
                                polarDeviceDisconnected(it.deviceName)
                            })
                        true
                    } else {
                        Napier.w(tag = "PolarHeartRateObservation::start") { "PolarHeartRateObservation: Could not find device with address: ${it.address}" }
                        false
                    }
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
        onCompletion()
    }

    override fun observerAccessible(): Boolean {
        if (MoreApplication.shared!!.showBleSetup().second) {
            val empty = MoreApplication.shared!!.mainBluetoothConnector.connected.isEmpty()
            if (empty) {
                MoreApplication.shared!!.coreBluetooth.enableBackgroundScanner()
            } else {
                MoreApplication.shared!!.coreBluetooth.disableBackgroundScanner()
            }
            return hasPermissions(MoreApplication.appContext!!) && !empty
        }
        return false
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
        Napier.d (tag = "PolarHeartRateObservation::hasPermission"){ "Polar has Bluetooth Permission!" }
        return true
    }

    companion object {
        val hrReady: MutableStateFlow<Boolean> = MutableStateFlow(false)

        fun polarDeviceDisconnected(specificName: String? = null) {
            MoreApplication.shared!!.mainBluetoothConnector.connected.filter { device ->
                device.deviceName?.lowercase()?.contains(specificName?.lowercase() ?: "polar") == true
            }.let {
                if (it.isEmpty()) {
                    hrReady.set(false)
                    MoreApplication.shared!!.observationManager.pauseObservationType(
                        PolarVerityHeartRateType(
                            emptySet()
                        ).observationType
                    )
                    Napier.d(tag = "PolarHeartRateObservation::Companion::polarDeviceDisconnected") { "HR Feature removed!" }
                }
            }
        }
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