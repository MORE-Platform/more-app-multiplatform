package io.redlink.more.more_app_mutliplatform.services.bluetooth

interface BluetoothConnectorObserver {

    fun isConnectingToDevice(bluetoothDevice: BluetoothDevice)

    fun didConnectToDevice(bluetoothDevice: BluetoothDevice)

    fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice)

    fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice)

    fun didDiscoverDevice(device: BluetoothDevice)

    fun removeDiscoveredDevice(device: BluetoothDevice)

    fun isScanning(boolean: Boolean)

    fun onBluetoothStateChange(bluetoothState: BluetoothState)
}