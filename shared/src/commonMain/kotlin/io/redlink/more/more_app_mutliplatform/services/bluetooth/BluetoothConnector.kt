package io.redlink.more.more_app_mutliplatform.services.bluetooth

interface BluetoothConnector {
    fun scan()

    fun connect(device: BluetoothDevice)

    fun disconnect(device: BluetoothDevice)

    fun stopScanning()
}