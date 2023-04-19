package io.redlink.more.more_app_mutliplatform.viewModels.bluetoothConnection

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreBluetoothConnectionViewModel(private val bluetoothConnector: BluetoothConnector): BluetoothConnectorObserver {

    val discoveredDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    val connectedDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())

    val isScanning = MutableStateFlow(false)

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    init {
        bluetoothConnector.observer = this
    }

    fun viewDidAppear() {
        bluetoothConnector.observer = this
        scanForDevices()
    }

    fun viewDidDisappear() {
        bluetoothConnector.close()
        scope.launch {
            isScanning.emit(bluetoothConnector.isScanning())
        }
    }

    fun scanForDevices() {
        bluetoothConnector.scan()
        scope.launch {
            isScanning.emit(bluetoothConnector.isScanning())
        }
    }

    fun stopScanning() {
        bluetoothConnector.stopScanning()
        scope.launch {
            isScanning.emit(bluetoothConnector.isScanning())
        }
    }

    fun connectToDevice(device: BluetoothDevice): Boolean {
        return bluetoothConnector.connect(device)?.let {
            print(it)
            false
        } ?: true
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        bluetoothConnector.disconnect(device)
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        Napier.i { "Connected to device $bluetoothDevice" }
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        Napier.i { "Disconnected from device $bluetoothDevice" }
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        Napier.e { "Failed to connect to $bluetoothDevice" }
    }

    override fun discoveredBLEUpdated(discoveredDevices: Set<BluetoothDevice>) {
        val context = this
        scope.launch {
            context.discoveredDevices.emit(discoveredDevices)
        }
    }

    override fun connectedBLEUpdated(connectedDevices: Set<BluetoothDevice>) {
        val context = this
        scope.launch {
            context.connectedDevices.emit(connectedDevices)
        }
    }

}