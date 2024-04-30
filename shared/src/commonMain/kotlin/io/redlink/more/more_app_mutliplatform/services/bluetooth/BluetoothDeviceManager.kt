package io.redlink.more.more_app_mutliplatform.services.bluetooth

import io.redlink.more.more_app_mutliplatform.extensions.appendAll
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.clear
import io.redlink.more.more_app_mutliplatform.extensions.removeAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


object BluetoothDeviceManager {
    private val _connectedDevices: MutableStateFlow<Set<BluetoothDevice>> =
        MutableStateFlow(emptySet())
    val connectedDevices: StateFlow<Set<BluetoothDevice>> = _connectedDevices
    private val _discoveredDevices: MutableStateFlow<Set<BluetoothDevice>> =
        MutableStateFlow(emptySet())
    val discoveredDevices: StateFlow<Set<BluetoothDevice>> = _discoveredDevices
    private val _pairedDevices: MutableStateFlow<Set<BluetoothDevice>> =
        MutableStateFlow(emptySet())
    val pairedDevices: StateFlow<Set<BluetoothDevice>> = _pairedDevices
    private val _devicesCurrentlyConnecting: MutableStateFlow<Set<BluetoothDevice>> =
        MutableStateFlow(
            emptySet()
        )

    val devicesCurrentlyConnecting: StateFlow<Set<BluetoothDevice>> = _devicesCurrentlyConnecting

    fun addConnectedDevices(devices: Set<BluetoothDevice>) {
        _connectedDevices.appendAll(devices)
        addPairedDeviceIds(devices)
        removeDiscoveredDevices(devices)
        removeConnectingDevices(devices)
    }

    fun removeConnectedDevices(devices: Set<BluetoothDevice>) {
        _connectedDevices.removeAll(devices)
    }

    fun addDiscoveredDevices(devices: Set<BluetoothDevice>) {
        _discoveredDevices.appendAll(devices)
    }

    fun removeDiscoveredDevices(devices: Set<BluetoothDevice>) {
        _discoveredDevices.removeAll(devices)
    }

    fun addPairedDeviceIds(deviceIds: Set<BluetoothDevice>) {
        _pairedDevices.appendAll(deviceIds)
    }

    fun removePairedDeviceIds(deviceIds: Set<BluetoothDevice>) {
        _pairedDevices.removeAll(deviceIds)
    }

    fun addConnectingDevices(devices: Set<BluetoothDevice>) {
        val connectedIds = devices.mapNotNull { it.address }.toSet()
        _devicesCurrentlyConnecting.appendAll(devices.filter { it.address !in connectedIds })
    }

    fun removeConnectingDevices(devices: Set<BluetoothDevice>) {
        _devicesCurrentlyConnecting.removeAll(devices)
    }

    fun connectedDevicesAsClosure(state: (Set<BluetoothDevice>) -> Unit) =
        this.connectedDevices.asClosure(state)

    fun connectedDevicesAsValue(): Set<BluetoothDevice> = connectedDevices.value

    fun discoveredDevicesAsClosure(state: (Set<BluetoothDevice>) -> Unit) =
        this.discoveredDevices.asClosure(state)

    fun pairedDeviceIdsAsClosure(state: (Set<BluetoothDevice>) -> Unit) =
        this.pairedDevices.asClosure(state)

    fun devicesCurrentlyConnectingAsClosure(state: (Set<BluetoothDevice>) -> Unit) =
        this.devicesCurrentlyConnecting.asClosure(state)

    fun foreachConnectedDevice(handler: (BluetoothDevice) -> Unit) {
        this.connectedDevices.value.forEach(handler)
    }

    fun foreachDiscoveredDevice(handler: (BluetoothDevice) -> Unit) {
        this.discoveredDevices.value.forEach(handler)
    }

    fun resetAll() {
        this._connectedDevices.clear()
        this._discoveredDevices.clear()
        this._devicesCurrentlyConnecting.clear()
    }

    fun clearCurrent() {
        this._connectedDevices.clear()
        this._discoveredDevices.clear()
    }
}