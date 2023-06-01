package io.redlink.more.app.android.services.bluetooth

import android.content.Context
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.reactivex.rxjava3.disposables.Disposable
import io.redlink.more.app.android.observations.HR.PolarConnectorListener
import io.redlink.more.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.app.android.observations.HR.PolarObserverCallback
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PolarConnector(context: Context): BluetoothConnector, PolarConnectorListener {
    private val polarObserverCallback: PolarObserverCallback = PolarObserverCallback()
    val polarApi: PolarBleApi by lazy {
        val api = PolarBleApiDefaultImpl.defaultImplementation(context, setOf(
            PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
            PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
            PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
        ))

        api.setPolarFilter(false)
        api.setApiCallback(polarObserverCallback)
        api.setAutomaticReconnection(true)
        api
    }

    private var scanDisposable: Disposable? = null

    override var scanning: Boolean = false
    override var observer: BluetoothConnectorObserver? = null

    override var bluetoothState: BluetoothState = BluetoothState.OFF

    override val connected: MutableSet<BluetoothDevice> = mutableSetOf()
    override val discovered: MutableSet<BluetoothDevice> = mutableSetOf()

    init {
        polarObserverCallback.connectionListener = this
    }

    override fun scan() {
        isScanning(true)
        scanDisposable = polarApi.searchForDevice()
            .subscribe(
                { polarDeviceInfo: PolarDeviceInfo ->
                    observer?.didDiscoverDevice(polarDeviceInfo.toBluetoothDevice())
                },
                { error: Throwable ->
                    Napier.e { error.stackTraceToString() }
                }
            )
    }

    override fun connect(device: BluetoothDevice): Error? {
        return try {
            device.deviceId?.let {
                polarApi.connectToDevice(it)
            }
            null
        } catch (error: Error) {
            Napier.e { error.stackTraceToString() }
            error
        }
    }

    override fun disconnect(device: BluetoothDevice) {
        try {
            device.deviceId?.let {
                polarApi.disconnectFromDevice(it)
            }
        } catch (error: Error) {
            Napier.e { error.stackTraceToString() }
        }
    }

    override fun stopScanning() {
        isScanning(false)
        scanDisposable?.dispose()
    }


    override fun onPolarFeatureReady(feature: PolarBleApi.PolarBleSdkFeature) {
        if (feature == PolarBleApi.PolarBleSdkFeature.FEATURE_HR) {
            CoroutineScope(Job() + Dispatchers.Default).launch {
                PolarHeartRateObservation.hrReady.emit(true)
            }
        }
    }

    override fun onDeviceConnected(polarDeviceInfo: PolarDeviceInfo) {
        observer?.didConnectToDevice(polarDeviceInfo.toBluetoothDevice())
    }
    override fun onDeviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            PolarHeartRateObservation.hrReady.emit(false)
        }
        observer?.didDisconnectFromDevice(polarDeviceInfo.toBluetoothDevice())
    }

    override fun onDeviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
        observer?.isConnectingToDevice(polarDeviceInfo.toBluetoothDevice())
    }

    override fun onPowerChange(bluetoothState: BluetoothState) {
        onBluetoothStateChange(bluetoothState)
    }

    override fun close() {
        log { "Closing PolarConnector..." }
        observer = null
        stopScanning()
        polarApi.cleanup()
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.isConnectingToDevice(bluetoothDevice)
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.didConnectToDevice(bluetoothDevice)
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        observer?.didDisconnectFromDevice(bluetoothDevice)
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.didFailToConnectToDevice(bluetoothDevice)
    }

    override fun didDiscoverDevice(device: BluetoothDevice) {
        observer?.didDiscoverDevice(device)
    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
        observer?.removeDiscoveredDevice(device)
    }

    override fun onBluetoothStateChange(bluetoothState: BluetoothState) {
        observer?.onBluetoothStateChange(bluetoothState)
    }

    override fun applyObserver(bluetoothConnectorObserver: BluetoothConnectorObserver?) {
        this.observer = bluetoothConnectorObserver
    }

    override fun replayStates() {
        connected.forEach { didConnectToDevice(it) }
        discovered.forEach { didDiscoverDevice(it) }
        isScanning(scanning)
    }

    override fun isScanning(boolean: Boolean) {
        this.scanning = boolean
        observer?.isScanning(boolean)
    }


}

fun PolarDeviceInfo.toBluetoothDevice(): BluetoothDevice {
    return BluetoothDevice.create(this.deviceId, this.name, this.address, this.isConnectable)
}