/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.services.bluetooth

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.github.aakira.napier.Napier
import io.redlink.more.database.entities.BluetoothDeviceEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BluetoothStateManagement {
    private val _bluetoothActive = MutableStateFlow(false)

    @NativeCoroutines
    val bluetoothActive: StateFlow<Boolean> = _bluetoothActive

    private val _scanning = MutableStateFlow(false)

    @NativeCoroutines
    val scanning: StateFlow<Boolean> = _scanning

    private val _connectedDevices: MutableStateFlow<Set<BluetoothDeviceEntity>> =
        MutableStateFlow(emptySet())

    @NativeCoroutines
    val connectedDevices: StateFlow<Set<BluetoothDeviceEntity>> = _connectedDevices
    private val _discoveredDevices: MutableStateFlow<Set<BluetoothDeviceEntity>> =
        MutableStateFlow(emptySet())

    @NativeCoroutines
    val discoveredDevices: StateFlow<Set<BluetoothDeviceEntity>> = _discoveredDevices
    private val _pairedDevices: MutableStateFlow<Set<BluetoothDeviceEntity>> =
        MutableStateFlow(emptySet())

    @NativeCoroutines
    val pairedDevices: StateFlow<Set<BluetoothDeviceEntity>> = _pairedDevices
    private val _devicesCurrentlyConnecting: MutableStateFlow<Set<BluetoothDeviceEntity>> =
        MutableStateFlow(
            emptySet()
        )

    @NativeCoroutines
    val devicesCurrentlyConnecting: StateFlow<Set<BluetoothDeviceEntity>> =
        _devicesCurrentlyConnecting

    private val _uiOverride = MutableStateFlow(false)

    @NativeCoroutines
    val uiOverride: StateFlow<Boolean> = _uiOverride

    private val _bgScanningActive = MutableStateFlow(false)

    @NativeCoroutines
    val bgScanningActive: StateFlow<Boolean> = _bgScanningActive

    fun setBluetoothState(active: Boolean) {
        _bluetoothActive.value = active
        if (!active) {
            resetAll()
        }
        Napier.i(tag = "BluetoothStateManagement::setBluetoothState") { "BLE powered on: $active" }
    }

    fun isScanning(scan: Boolean) {
        _scanning.value = scan

        Napier.i(tag = "BluetoothStateManagement::isScanning") { "BLE scanning active: $scan" }
    }

    fun uiOverrides(override: Boolean) {
        _uiOverride.value = override
        Napier.i { "BLE UI overrides: $override" }
    }

    fun enableBgScanning(): Boolean {
        if (bgScanningActive.value) {
            return false
        }
        _bgScanningActive.value = true
        Napier.i { "BLE Background Scan enabled" }
        return true
    }

    fun disableBgScanning() {
        if (bgScanningActive.value) {
            Napier.i { "BLE Background Scan disabled" }
        }
        _bgScanningActive.value = false
    }

    fun addConnectedDevices(devices: Set<BluetoothDeviceEntity>) {
        _connectedDevices.value += devices
        addPairedDeviceIds(devices.filter { it !in pairedDevices.value }.toSet())
        removeDiscoveredDevices(devices)
        removeConnectingDevices(devices)
    }

    fun removeConnectedDevices(devices: Set<BluetoothDeviceEntity>) {
        _connectedDevices.value -= devices
        _discoveredDevices.value -= devices
        _connectedDevices.value -= devices
    }

    fun addDiscoveredDevices(devices: Set<BluetoothDeviceEntity>) {
        _discoveredDevices.value += devices.filter { !connectedDevices.value.contains(it) }
    }

    fun removeDiscoveredDevices(devices: Set<BluetoothDeviceEntity>) {
        _discoveredDevices.value -= devices
    }

    fun addPairedDeviceIds(deviceIds: Set<BluetoothDeviceEntity>) {
        _pairedDevices.value += deviceIds
    }

    fun removePairedDeviceIds(deviceIds: Set<BluetoothDeviceEntity>) {
        _pairedDevices.value -= deviceIds
    }

    fun addConnectingDevices(devices: Set<BluetoothDeviceEntity>) {
        _devicesCurrentlyConnecting.value += devices.filter { !connectedDevices.value.contains(it) }
    }

    fun removeConnectingDevices(devices: Set<BluetoothDeviceEntity>) {
        _devicesCurrentlyConnecting.value -= devices
    }

    // Used in iOS
    fun removeDiscoveredDeviceIds(deviceIds: Set<String>) {
        _discoveredDevices.value =
            discoveredDevices.value.filterTo(mutableSetOf()) { it.deviceId !in deviceIds }
    }

    // Used in iOS
    fun removeConnectingDeviceIds(deviceIds: Set<String>) {
        _devicesCurrentlyConnecting.value =
            devicesCurrentlyConnecting.value.filterTo(mutableSetOf()) { it.deviceId !in deviceIds }
    }

    // Used in iOS
    fun removeConnectedDeviceIds(deviceIds: Set<String>) {
        _connectedDevices.value =
            connectedDevices.value.filterTo(mutableSetOf()) { it.deviceId !in deviceIds }
    }

    fun resetAll() {
        clearDiscovered()
        clearConnected()
        clearConnectingDevices()
        _scanning.value = false
        _bgScanningActive.value = false
    }

    fun clearDiscovered() {
        this._discoveredDevices.value = setOf()
    }

    fun clearConnected() {
        this._connectedDevices.value = setOf()
    }

    fun clearConnectingDevices() {
        this._devicesCurrentlyConnecting.value = setOf()
    }
}