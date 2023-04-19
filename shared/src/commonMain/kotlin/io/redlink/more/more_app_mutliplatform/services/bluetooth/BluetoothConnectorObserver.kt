package io.redlink.more.more_app_mutliplatform.services.bluetooth

interface BluetoothConnectorObserver {
    fun didConnectToDevice(bluetoothDevice: BluetoothDevice)

    fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice)

    fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice)

    fun discoveredBLEUpdated(discoveredDevices: Set<BluetoothDevice>)

    fun connectedBLEUpdated(connectedDevices: Set<BluetoothDevice>)
}