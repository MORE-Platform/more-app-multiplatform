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
import io.redlink.more.app.android.observations.HR.PolarObserverCallback
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.services.bluetooth.BluetoothConnector
import io.redlink.more.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.services.bluetooth.BluetoothState
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import io.redlink.more.services.bluetooth.polar.PolarStates

class PolarConnector(context: Context) : BluetoothConnector, PolarConnectorListener {
    private val polarObserverCallback: PolarObserverCallback = PolarObserverCallback()
    val polarApi: PolarBleApi = PolarBleApiDefaultImpl.defaultImplementation(
        context, setOf(
            PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
            PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
            PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
        )
    ).apply {
        setPolarFilter(true)
        setApiCallback(polarObserverCallback)
        setAutomaticReconnection(true)
    }

    private val bleManager = BluetoothStateManagement

    private var scanDisposable: Disposable? = null

    override val specificBluetoothConnectors: MutableMap<String, BluetoothConnector> =
        mutableMapOf()
    override var observer: MutableSet<BluetoothConnectorObserver> = mutableSetOf()

    init {
        polarObserverCallback.connectionListener = this
        (MoreApplication.appContext!!.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter?.let {
            val state = when (it.state) {
                BluetoothAdapter.STATE_ON -> BluetoothState.ON
                BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.ON
                else -> BluetoothState.OFF
            }
            bleManager.setBluetoothState(state == BluetoothState.ON)
        }
    }

    override fun scan() {
        if (!bleManager.scanning.value && observer.isNotEmpty() && bleManager.bluetoothActive.value) {
            bleManager.isScanning(true)
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

    override fun connect(device: BluetoothDeviceEntity): Error? {
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

    override fun disconnect(device: BluetoothDeviceEntity) {
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
        if (bleManager.scanning.value) {
            scanDisposable?.dispose()
            bleManager.isScanning(false)
        }
    }

    override fun onPolarFeatureReady(feature: PolarBleApi.PolarBleSdkFeature) {
        if (feature == PolarBleApi.PolarBleSdkFeature.FEATURE_HR) {
            Napier.i(tag = "PolarConnector::onPolarFeatureReady") { "HR ready!" }
            PolarStates.hrFeatureReady(true)

            Napier.d(tag = "PolarHeartRateObservation:::setHRFeature") { "HR Feature Ready!" }
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

    override fun close() {
        Napier.i(tag = "PolarConnector::close") { "Closing PolarConnector..." }
        stopScanning()
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        updateObserver { it.isConnectingToDevice(bluetoothDevice) }
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        updateObserver { it.didConnectToDevice(bluetoothDevice) }
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDeviceEntity) {
        updateObserver { it.didDisconnectFromDevice(bluetoothDevice) }
        if (bleManager.connectedDevices.value.map { it.deviceName?.contains("polar") }.isEmpty()) {
            PolarStates.hrFeatureReady(false)
        }
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        updateObserver {
            it.didFailToConnectToDevice(bluetoothDevice)
        }
    }

    override fun didDiscoverDevice(device: BluetoothDeviceEntity) {
        Napier.i { "Device Discovered: $device" }
        updateObserver {
            it.didDiscoverDevice(device)
        }
    }

    override fun removeDiscoveredDevice(device: BluetoothDeviceEntity) {
        updateObserver {
            it.removeDiscoveredDevice(device)
        }
    }

    override fun resetAll() {
        polarApi.cleanup()
    }

    override fun addObserver(bluetoothConnectorObserver: BluetoothConnectorObserver) {
        this.observer.add(bluetoothConnectorObserver)
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

    override fun addSpecificBluetoothConnector(key: String, connector: BluetoothConnector) {
        specificBluetoothConnectors[key] = connector
    }
}

fun PolarDeviceInfo.toBluetoothDevice(): BluetoothDeviceEntity =
    BluetoothDeviceEntity.create(this.deviceId, this.name, this.address)