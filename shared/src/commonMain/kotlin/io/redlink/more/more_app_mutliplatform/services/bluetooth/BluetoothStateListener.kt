package io.redlink.more.more_app_mutliplatform.services.bluetooth

interface BluetoothStateListener {
    fun onBluetoothStateChange(bluetoothState: BluetoothState)
}