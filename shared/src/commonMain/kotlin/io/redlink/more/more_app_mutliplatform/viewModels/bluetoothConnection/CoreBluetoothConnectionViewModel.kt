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
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.util.Scope.repeatedLaunch
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

class CoreBluetoothConnectionViewModel(
    private val bluetoothConnector: BluetoothConnector,
    private val scanDuration: Long = 10000,
    private val scanInterval: Long = 5000
): CoreViewModel(), BluetoothConnectorObserver, Closeable {
    private val bluetoothDeviceRepository = BluetoothDeviceRepository(bluetoothConnector)
    val discoveredDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    val connectedDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    val connectingDevices = MutableStateFlow<Set<String>>(emptySet())

    val isScanning = MutableStateFlow(false)

    private var scanJob: String? = null

    val bluetoothPower = MutableStateFlow(BluetoothState.ON)

    override fun viewDidAppear() {
        bluetoothConnector.applyObserver(this)
        launchScope {
            bluetoothPower.collect {
                if (it == BluetoothState.ON) {
                    startPeriodicScan()
                } else {
                    stopPeriodicScan()
                }
            }
        }
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        scanJob?.let { Scope.cancel(it) }
        scanJob = null
        discoveredDevices.clear()
        connectingDevices.clear()
        close()
    }

    private fun startPeriodicScan() {
        if (scanJob == null) {
            scanJob = repeatedLaunch(scanInterval) {
                scanForDevices()
                delay(scanDuration)
                stopScanning()
            }.first
        }
    }

    fun stopPeriodicScan() {
        close()
    }

    fun scanForDevices() {
        bluetoothConnector.scan()
    }

    fun stopScanning() {
        bluetoothConnector.stopScanning()
    }

    fun connectToDevice(device: BluetoothDevice): Boolean {
        Napier.i { "Connecting to $device" }
        connectingDevices.append(device.address)
        return bluetoothConnector.connect(device)?.let {
            connectingDevices.remove(device.address)
            print(it)
            false
        } ?: true
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        Napier.i { "Disconnecting from $device" }
        bluetoothConnector.disconnect(device)
        connectedDevices.removeWhere { it.address == device.address }
        bluetoothDeviceRepository.setConnectionState(device, false)
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
            bluetoothDeviceRepository.setConnectionState(bluetoothDevice, true)
        }
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        connectedDevices.removeWhere { it.address == bluetoothDevice.address }
        bluetoothDeviceRepository.setConnectionState(bluetoothDevice, false)
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        connectedDevices.remove(bluetoothDevice)
        connectingDevices.remove(bluetoothDevice.address)
        bluetoothDeviceRepository.setConnectionState(bluetoothDevice, false)
        Napier.e { "Failed to connect to $bluetoothDevice" }
    }

    override fun onBluetoothStateChange(bluetoothState: BluetoothState) {
        bluetoothPower.set(bluetoothState)
    }

    override fun didDiscoverDevice(device: BluetoothDevice) {
        if (!connectedDevices.value.mapNotNull { it.address }.contains(device.address)
            && !discoveredDevices.value.mapNotNull { it.address }.contains(device.address)) {
            discoveredDevices.append(device)
        }
    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
        discoveredDevices.removeWhere { it.address == device.address }
        connectingDevices.remove(device.address)
    }

    override fun isScanning(boolean: Boolean) {
        this.isScanning.set(boolean)
    }

    fun discoveredDevicesListChanges(providedState: (Set<BluetoothDevice>) -> Unit) =
        discoveredDevices.asClosure(providedState)

    fun connectedDevicesListChanges(providedState: (Set<BluetoothDevice>) -> Unit) =
        connectedDevices.asClosure(providedState)

    fun scanningIsChanging(providedState: (Boolean) -> Unit) = isScanning.asClosure(providedState)

    fun connectingDevicesListChanges(providedState: (Set<String>) -> Unit) =
        connectingDevices.asClosure(providedState)

    fun bluetoothStateChanged(providedState: (BluetoothState) -> Unit) = bluetoothPower.asClosure(providedState)

    override fun close() {
        scanJob?.let { Scope.cancel(it) }
        scanJob = null
        bluetoothConnector.stopScanning()
        bluetoothConnector.applyObserver(null)
    }
}