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
package io.redlink.more.app.android.services.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import io.github.aakira.napier.Napier
import io.reactivex.rxjava3.disposables.Disposable
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.observations.HR.PolarConnectorListener
import io.redlink.more.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.app.android.observations.HR.PolarObserverCallback
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.delay

class PolarConnector(context: Context) : BluetoothConnector, PolarConnectorListener {
    private val polarObserverCallback: PolarObserverCallback = PolarObserverCallback()
    val polarApi: PolarBleApi by lazy {
        val api = PolarBleApiDefaultImpl.defaultImplementation(
            context, setOf(
                PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
                PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
            )
        )

        api.setPolarFilter(true)
        api.setApiCallback(polarObserverCallback)
        api.setAutomaticReconnection(true)
        api
    }

    private var scanDisposable: Disposable? = null

    override val specificBluetoothConnectors: MutableMap<String, BluetoothConnector> =
        mutableMapOf()
    override var scanning: Boolean = false
    override var observer: MutableSet<BluetoothConnectorObserver> = mutableSetOf()

    override var bluetoothState: BluetoothState = BluetoothState.OFF

    init {
        polarObserverCallback.connectionListener = this
        (MoreApplication.appContext!!.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter?.let {
            this.bluetoothState = when (it.state) {
                BluetoothAdapter.STATE_ON -> BluetoothState.ON
                BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.ON
                else -> BluetoothState.OFF
            }
            Napier.i(tag = "PolarConnector::init") { "Current Bluetooth State: $bluetoothState" }
        }
    }

    override fun scan() {
        if (!scanning && observer.isNotEmpty() && bluetoothState == BluetoothState.ON) {
            isScanning(true)
            Napier.i(tag = "PolarConnector::scan") { "Scanning started." }
            scanDisposable = polarApi.searchForDevice()
                .subscribe(
                    { polarDeviceInfo: PolarDeviceInfo ->
                        didDiscoverDevice(polarDeviceInfo.toBluetoothDevice())
                    },
                    { error: Throwable ->
                        Napier.e(tag = "PolarConnector::scan") { error.stackTraceToString() }
                    }
                )
        }
    }

    override fun connect(device: BluetoothDevice): Error? {
        Napier.i(tag = "PolarConnector::connect") { "Connecting to device: $device" }
        return try {
            device.address?.let {
                polarApi.connectToDevice(it)
            }
            null
        } catch (error: Error) {
            Napier.e(tag = "PolarConnector::connect") { error.stackTraceToString() }
            error
        }
    }

    override fun disconnect(device: BluetoothDevice) {
        Napier.i(tag = "PolarConnector::disconnect") { "Disconnecting from device: $device" }
        try {
            device.address?.let {
                polarApi.disconnectFromDevice(it)
            }
        } catch (error: Error) {
            Napier.e(tag = "PolarConnector::disconnect") { error.stackTraceToString() }
        }
    }

    override fun stopScanning() {
        Napier.i(tag = "PolarConnector::stopScanning") { "Stopping scanning." }
        if (scanning) {
            scanDisposable?.dispose()
            polarApi.cleanup()
            isScanning(false)
        }
    }


    override fun onPolarFeatureReady(feature: PolarBleApi.PolarBleSdkFeature) {
        if (feature == PolarBleApi.PolarBleSdkFeature.FEATURE_HR) {
            Napier.i(tag = "PolarConnector::onPolarFeatureReady") { "HR ready!" }
            PolarHeartRateObservation.setHRFeature(false)
        }
    }

    override fun onDeviceConnected(polarDeviceInfo: PolarDeviceInfo) {
        Napier.i(tag = "PolarConnector::onDeviceConnected") { "Device connected: $polarDeviceInfo" }
        didConnectToDevice(polarDeviceInfo.toBluetoothDevice())
    }

    override fun onDeviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
        Napier.i(tag = "PolarConnector::onDeviceDisconnected") { "Device disconnected: $polarDeviceInfo" }
        didDisconnectFromDevice(polarDeviceInfo.toBluetoothDevice())
    }

    override fun onDeviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
        isConnectingToDevice(polarDeviceInfo.toBluetoothDevice())
    }

    override fun onPowerChange(bluetoothState: BluetoothState) {
        Napier.i(tag = "PolarConnector::onPowerChange") { "Bluetooth power change: $bluetoothState" }
        onBluetoothStateChange(bluetoothState)
    }

    override fun close() {
        Napier.i(tag = "PolarConnector::close") { "Closing PolarConnector..." }
        stopScanning()
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
        updateObserver { it.isConnectingToDevice(bluetoothDevice) }
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        updateObserver { it.didConnectToDevice(bluetoothDevice) }
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        updateObserver { it.didDisconnectFromDevice(bluetoothDevice) }
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        updateObserver {
            it.didFailToConnectToDevice(bluetoothDevice)
        }
    }

    override fun didDiscoverDevice(device: BluetoothDevice) {
        Napier.i { "Device Discovered: $device" }
        updateObserver {
            it.didDiscoverDevice(device)
        }
    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
        updateObserver {
            it.removeDiscoveredDevice(device)
        }
    }

    override fun onBluetoothStateChange(bluetoothState: BluetoothState) {
        this.bluetoothState = bluetoothState
        updateObserver { it.onBluetoothStateChange(bluetoothState) }
        if (MoreApplication.shared?.credentialRepository?.hasCredentials() == true && bluetoothState == BluetoothState.ON) {
            Scope.launch {
                scan()
                delay(10000)
                stopScanning()
            }
        } else {
            stopScanning()
        }
    }

    override fun addObserver(bluetoothConnectorObserver: BluetoothConnectorObserver) {
        this.observer.add(bluetoothConnectorObserver)
        if (this.observer.isNotEmpty()) {
            replayStates()
        }
    }

    override fun removeObserver(bluetoothConnectorObserver: BluetoothConnectorObserver) {
        this.observer.remove(bluetoothConnectorObserver)
        if (this.observer.isEmpty()) {
            stopScanning()
        }
    }

    override fun updateObserver(action: (BluetoothConnectorObserver) -> Unit) {
        if (observer.isEmpty()) {
            Napier.d(tag = "PolarConnector::updateObserver") { "No observer available to send data to!" }
        } else {
            Napier.d(tag = "PolarConnector::updateObserver") { "Sending data to observer..." }
        }
        observer.forEach(action)
    }

    override fun replayStates() {
        onBluetoothStateChange(bluetoothState)
        isScanning(scanning)
    }

    override fun isScanning(boolean: Boolean) {
        this.scanning = boolean
        updateObserver { it.isScanning(boolean) }
    }

    override fun addSpecificBluetoothConnector(key: String, connector: BluetoothConnector) {
        specificBluetoothConnectors[key] = connector
    }
}

fun PolarDeviceInfo.toBluetoothDevice(): BluetoothDevice {
    return BluetoothDevice.create(this.deviceId, this.name, this.address)
}