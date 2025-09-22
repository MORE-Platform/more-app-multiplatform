package io.redlink.more.more_app_mutliplatform.services.bluetooth

import io.redlink.more.more_app_mutliplatform.database.entities.BluetoothDeviceEntity
import io.redlink.more.more_app_mutliplatform.extensions.appendAll
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.clear
import io.redlink.more.more_app_mutliplatform.extensions.removeAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BluetoothDeviceManager {
    private val _connectedDevices: MutableStateFlow<Set<BluetoothDeviceEntity>> =
        MutableStateFlow(emptySet())
    val connectedDevices: StateFlow<Set<BluetoothDeviceEntity>> = _connectedDevices
    private val _discoveredDevices: MutableStateFlow<Set<BluetoothDeviceEntity>> =
        MutableStateFlow(emptySet())
    val discoveredDevices: StateFlow<Set<BluetoothDeviceEntity>> = _discoveredDevices
    private val _pairedDevices: MutableStateFlow<Set<BluetoothDeviceEntity>> =
        MutableStateFlow(emptySet())
    val pairedDevices: StateFlow<Set<BluetoothDeviceEntity>> = _pairedDevices
    private val _devicesCurrentlyConnecting: MutableStateFlow<Set<BluetoothDeviceEntity>> =
        MutableStateFlow(
            emptySet()
        )

    val devicesCurrentlyConnecting: StateFlow<Set<BluetoothDeviceEntity>> =
        _devicesCurrentlyConnecting

    fun addConnectedDevices(devices: Set<BluetoothDeviceEntity>) {
        _connectedDevices.appendAll(devices)
        addPairedDeviceIds(devices.filter { it !in pairedDevices.value }.toSet())
        removeDiscoveredDevices(devices)
        removeConnectingDevices(devices)
    }

    fun removeConnectedDevices(devices: Set<BluetoothDeviceEntity>) {
        _connectedDevices.removeAll(devices)
        _discoveredDevices.removeAll(devices)
    }

    fun addDiscoveredDevices(devices: Set<BluetoothDeviceEntity>) {
        _discoveredDevices.appendAll(devices.filter { !connectedDevices.value.contains(it) })
    }

    fun removeDiscoveredDevices(devices: Set<BluetoothDeviceEntity>) {
        _discoveredDevices.removeAll(devices)
    }

    fun addPairedDeviceIds(deviceIds: Set<BluetoothDeviceEntity>) {
        _pairedDevices.appendAll(deviceIds)
    }

    fun removePairedDeviceIds(deviceIds: Set<BluetoothDeviceEntity>) {
        _pairedDevices.removeAll(deviceIds)
    }

    fun addConnectingDevices(devices: Set<BluetoothDeviceEntity>) {
        _devicesCurrentlyConnecting.appendAll(devices.filter { !connectedDevices.value.contains(it) })
    }

    fun removeConnectingDevices(devices: Set<BluetoothDeviceEntity>) {
        _devicesCurrentlyConnecting.removeAll(devices)
    }

    fun connectedDevicesAsClosure(state: (Set<BluetoothDeviceEntity>) -> Unit) =
        this.connectedDevices.asClosure(state)

    fun connectedDevicesAsValue(): Set<BluetoothDeviceEntity> = connectedDevices.value

    fun discoveredDevicesAsClosure(state: (Set<BluetoothDeviceEntity>) -> Unit) =
        this.discoveredDevices.asClosure(state)

    fun pairedDeviceIdsAsClosure(state: (Set<BluetoothDeviceEntity>) -> Unit) =
        this.pairedDevices.asClosure(state)

    fun devicesCurrentlyConnectingAsClosure(state: (Set<BluetoothDeviceEntity>) -> Unit) =
        this.devicesCurrentlyConnecting.asClosure(state)

    fun foreachConnectedDevice(handler: (BluetoothDeviceEntity) -> Unit) {
        this.connectedDevices.value.forEach(handler)
    }

    fun foreachDiscoveredDevice(handler: (BluetoothDeviceEntity) -> Unit) {
        this.discoveredDevices.value.forEach(handler)
    }

    fun resetAll() {
        this._discoveredDevices.clear()
    }

    fun clearDiscovered() {
        this._discoveredDevices.clear()
    }

    fun clearConnected() {
        this._connectedDevices.clear()
    }

    fun clearConnectingDevices() {
        this._devicesCurrentlyConnecting.clear()
    }
}