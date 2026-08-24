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
package io.redlink.more.mocks

import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.services.bluetooth.BluetoothConnector
import io.redlink.more.services.bluetooth.BluetoothConnectorObserver

class MockBluetoothConnector : BluetoothConnector {
    override var observer: MutableSet<BluetoothConnectorObserver> = mutableSetOf()
    override val specificBluetoothConnectors: MutableMap<String, BluetoothConnector> =
        mutableMapOf()

    override fun addSpecificBluetoothConnector(key: String, connector: BluetoothConnector) {}
    override fun addObserver(bluetoothConnectorObserver: BluetoothConnectorObserver) {}
    override fun removeObserver(bluetoothConnectorObserver: BluetoothConnectorObserver) {}
    override fun updateObserver(action: (BluetoothConnectorObserver) -> Unit) {}
    override fun scan() {}
    override fun connect(device: BluetoothDeviceEntity): Error? = null
    override fun disconnect(device: BluetoothDeviceEntity) {}
    override fun stopScanning() {}
    override fun close() {}
    override fun isConnectingToDevice(bluetoothDevice: BluetoothDeviceEntity) {}
    override fun didConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {}
    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDeviceEntity) {}
    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {}
    override fun didDiscoverDevice(device: BluetoothDeviceEntity) {}
    override fun removeDiscoveredDevice(device: BluetoothDeviceEntity) {}
    override fun resetAll() {}
}