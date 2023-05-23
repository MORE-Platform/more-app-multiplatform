package io.redlink.more.more_app_mutliplatform.viewModels.bluetoothConnection

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.database.repository.BluetoothDeviceRepository
import io.redlink.more.more_app_mutliplatform.extensions.append
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.clear
import io.redlink.more.more_app_mutliplatform.extensions.remove
import io.redlink.more.more_app_mutliplatform.extensions.removeWhere
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.util.Scope.launch
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable

class CoreBluetoothConnectionViewModel(private val bluetoothConnector: BluetoothConnector) :
    CoreViewModel(), BluetoothConnectorObserver, Closeable {
    private val bluetoothDeviceRepository = BluetoothDeviceRepository(bluetoothConnector)
    val discoveredDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    val connectedDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    val connectingDevices = MutableStateFlow<Set<String>>(emptySet())

    val isScanning = MutableStateFlow(false)
    private val isConnecting = MutableStateFlow(false)

    private var scanJob: Job? = null
    private val scanDuration: Long = 10000
    private val scanInterval: Long = 5000

    init {
        bluetoothConnector.observer = this
    }

    override fun viewDidAppear() {
        launchScope {
            bluetoothDeviceRepository.getConnectedDevices(true).cancellable().collect { storedConnectedDevices ->
                val addresses = connectedDevices.value.map { it.address }
                val filtered = storedConnectedDevices.filter { it.address !in addresses }
                connectedDevices.append(filtered)
            }
        }
        startPeriodicScan()
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        discoveredDevices.clear()
        connectingDevices.clear()
        close()
    }

    private fun startPeriodicScan() {
        if (scanJob == null) {
            scanJob = launch(Dispatchers.Default) {
                while (true) {
                    scanForDevices()
                    delay(scanDuration)
                    stopScanning()
                    delay(scanInterval)
                }
            }.second
        }
    }

    fun stopPeriodicScan() {
        scanJob?.cancel()
        scanJob = null
        close()
    }

    fun scanForDevices() {
        if (!isConnecting.value) {
            bluetoothConnector.scan()
            launchScope {
                isScanning.emit(bluetoothConnector.isScanning())
            }
        }
    }

    fun stopScanning() {
        bluetoothConnector.stopScanning()
        launch {
            isScanning.emit(bluetoothConnector.isScanning())
        }
    }

    fun connectToDevice(device: BluetoothDevice): Boolean {
        Napier.i { "Connecting to device" }
        connectingDevices.append(device.address)
        return bluetoothConnector.connect(device)?.let {
            connectingDevices.remove(device.address)
            print(it)
            false
        } ?: true
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        bluetoothConnector.disconnect(device)
        discoveredDevices.append(device)
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
        if (bluetoothDevice.address !in connectedDevices.value.mapNotNull { it.address }) {
            connectingDevices.append(bluetoothDevice.address)
        }
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        if (!connectedDevices.value.mapNotNull { it.address }.contains(bluetoothDevice.address)) {
            connectedDevices.append(bluetoothDevice)
            connectingDevices.remove(bluetoothDevice.address)
            discoveredDevices.removeWhere { it.address == bluetoothDevice.address }
            bluetoothDeviceRepository.setConnectionState(bluetoothDevice, true)
            isConnecting.set(false)
        }
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        connectedDevices.removeWhere { it.address == bluetoothDevice.address }
        bluetoothDeviceRepository.setConnectionState(bluetoothDevice, false)
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        connectedDevices.remove(bluetoothDevice)
        connectingDevices.remove(bluetoothDevice.address)
        isConnecting.set(false)
        Napier.e { "Failed to connect to $bluetoothDevice" }
    }

    override fun discoveredDevice(device: BluetoothDevice) {
        if (!connectedDevices.value.mapNotNull { it.address }
                .contains(device.address) && !discoveredDevices.value.mapNotNull { it.address }
                .contains(device.address)) {
            discoveredDevices.append(device)
        }
    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
        discoveredDevices.removeWhere { it.address == device.address }
        connectingDevices.remove(device.address)
    }

    fun discoveredDevicesListChanges(providedState: (Set<BluetoothDevice>) -> Unit) =
        discoveredDevices.asClosure(providedState)

    fun connectedDevicesListChanges(providedState: (Set<BluetoothDevice>) -> Unit) =
        connectedDevices.asClosure(providedState)

    fun scanningIsChanging(providedState: (Boolean) -> Unit) = isScanning.asClosure(providedState)

    fun connectingDevicesListChanges(providedState: (Set<String>) -> Unit) =
        connectingDevices.asClosure(providedState)

    override fun close() {
        scanJob?.cancel()
        scanJob = null
        bluetoothConnector.stopScanning()
        isConnecting.set(false)
        launch {
            isScanning.emit(bluetoothConnector.isScanning())
        }
    }
}