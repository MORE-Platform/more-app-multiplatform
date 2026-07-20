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