package io.redlink.more.more_app_mutliplatform.services.bluetooth

import io.redlink.more.more_app_mutliplatform.extensions.appendAll
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.clear
import io.redlink.more.more_app_mutliplatform.extensions.removeAll
import kotlinx.coroutines.flow.MutableStateFlow

object BluetoothDeviceManager {
    val connectedDevices: MutableStateFlow<Set<BluetoothDevice>> = MutableStateFlow(emptySet())
    val discoveredDevices: MutableStateFlow<Set<BluetoothDevice>> = MutableStateFlow(emptySet())
    val pairedDevices: MutableStateFlow<Set<BluetoothDevice>> = MutableStateFlow(emptySet())
    val devicesCurrentlyConnecting: MutableStateFlow<Set<BluetoothDevice>> = MutableStateFlow(
        emptySet()
    )

    fun addConnectedDevices(devices: Set<BluetoothDevice>) {
        connectedDevices.appendAll(devices)
        addPairedDeviceIds(devices)
        removeDiscoveredDevices(devices)
        removeConnectingDevices(devices)
    }

    fun removeConnectedDevices(devices: Set<BluetoothDevice>) {
        connectedDevices.removeAll(devices)
    }

    fun addDiscoveredDevices(devices: Set<BluetoothDevice>) {
        discoveredDevices.appendAll(devices)
    }

    fun removeDiscoveredDevices(devices: Set<BluetoothDevice>) {
        discoveredDevices.removeAll(devices)
    }

    fun addPairedDeviceIds(deviceIds: Set<BluetoothDevice>) {
        pairedDevices.appendAll(deviceIds)
    }

    fun removePairedDeviceIds(deviceIds: Set<BluetoothDevice>) {
        pairedDevices.removeAll(deviceIds)
    }

    fun addConnectingDevices(devices: Set<BluetoothDevice>) {
        val connectedIds = devices.mapNotNull { it.address }.toSet()
        devicesCurrentlyConnecting.appendAll(devices.filter { it.address !in connectedIds })
    }

    fun removeConnectingDevices(devices: Set<BluetoothDevice>) {
        devicesCurrentlyConnecting.removeAll(devices)
    }

    fun connectedDevicesAsClosure(state: (Set<BluetoothDevice>) -> Unit) =
        this.connectedDevices.asClosure(state)

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
        this.connectedDevices.clear()
        this.discoveredDevices.clear()
        this.devicesCurrentlyConnecting.clear()
        this.pairedDevices.clear()
    }
}