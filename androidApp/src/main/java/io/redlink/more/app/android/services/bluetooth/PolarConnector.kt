package io.redlink.more.app.android.services.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
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

        api.setPolarFilter(false)
        api.setApiCallback(polarObserverCallback)
        api.setAutomaticReconnection(true)
        api.setApiLogger { str -> Napier.d { str } }
        api
    }

    private var scanDisposable: Disposable? = null

    override val specificBluetoothConnectors: MutableMap<String, BluetoothConnector> =
        mutableMapOf()
    override var scanning: Boolean = false
    override var observer: MutableSet<BluetoothConnectorObserver> = mutableSetOf()

    override var bluetoothState: BluetoothState = BluetoothState.OFF

    override val connected: MutableSet<BluetoothDevice> = mutableSetOf()
    override val discovered: MutableSet<BluetoothDevice> = mutableSetOf()

    init {
        polarObserverCallback.connectionListener = this
        (MoreApplication.appContext!!.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter?.let {
            this.bluetoothState = when (it.state) {
                BluetoothAdapter.STATE_ON -> BluetoothState.ON
                BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.ON
                else -> BluetoothState.OFF
            }
            Napier.d { "Current Bluetooth State: $bluetoothState" }
        }
    }

    override fun scan() {
        if (!scanning && observer.isNotEmpty() && bluetoothState == BluetoothState.ON) {
            isScanning(true)
            scanDisposable = polarApi.searchForDevice()
                .subscribe(
                    { polarDeviceInfo: PolarDeviceInfo ->
                        didDiscoverDevice(polarDeviceInfo.toBluetoothDevice())
                    },
                    { error: Throwable ->
                        Napier.e { error.stackTraceToString() }
                    }
                )
        }
    }

    override fun connect(device: BluetoothDevice): Error? {
        return try {
            device.address?.let {
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
            device.address?.let {
                polarApi.disconnectFromDevice(it)
            }
        } catch (error: Error) {
            Napier.e { error.stackTraceToString() }
        }
    }

    override fun stopScanning() {
        if (scanning) {
            isScanning(false)
            scanDisposable?.dispose()
            polarApi.cleanup()
        }
    }


    override fun onPolarFeatureReady(feature: PolarBleApi.PolarBleSdkFeature) {
        if (feature == PolarBleApi.PolarBleSdkFeature.FEATURE_HR) {
            Napier.d { "HR ready!" }
            PolarHeartRateObservation.setHRReady()
        }
    }

    override fun onDeviceConnected(polarDeviceInfo: PolarDeviceInfo) {
        didConnectToDevice(polarDeviceInfo.toBluetoothDevice())
    }

    override fun onDeviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
        didDisconnectFromDevice(polarDeviceInfo.toBluetoothDevice())
    }

    override fun onDeviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
        isConnectingToDevice(polarDeviceInfo.toBluetoothDevice())
    }

    override fun onPowerChange(bluetoothState: BluetoothState) {
        onBluetoothStateChange(bluetoothState)
    }

    override fun close() {
        log { "Closing PolarConnector..." }
        stopScanning()
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
        updateObserver { it.isConnectingToDevice(bluetoothDevice) }
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        if (connected.none { it.address == bluetoothDevice.address }) {
            connected.add(bluetoothDevice)
            removeDiscoveredDevice(bluetoothDevice)
        }
        updateObserver { it.didConnectToDevice(bluetoothDevice) }
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        connected.removeAll { bluetoothDevice.address == it.address }
        PolarHeartRateObservation.polarDeviceDisconnected()
        updateObserver { it.didDisconnectFromDevice(bluetoothDevice) }
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        updateObserver {
            it.didFailToConnectToDevice(bluetoothDevice)
        }
    }

    override fun didDiscoverDevice(device: BluetoothDevice) {
        Napier.d { "Device Discovered: $device" }
        if (discovered.none { it.address == device.address } && connected.none { it.address == device.address }) {
            discovered.add(device)
        }
        updateObserver {
            it.didDiscoverDevice(device)
        }
    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
        discovered.removeAll { it.address == device.address }
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
            connected.forEach {
                didDisconnectFromDevice(it)
            }
            discovered.forEach {
                removeDiscoveredDevice(it)
            }
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
        observer.forEach(action)
    }

    override fun replayStates() {
        onBluetoothStateChange(bluetoothState)
        connected.forEach { didConnectToDevice(it) }
        discovered.forEach { didDiscoverDevice(it) }
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
    return BluetoothDevice.create(this.deviceId, this.name, this.address, this.isConnectable)
}