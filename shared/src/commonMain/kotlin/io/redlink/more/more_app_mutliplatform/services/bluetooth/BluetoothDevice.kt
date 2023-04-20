package io.redlink.more.more_app_mutliplatform.services.bluetooth

data class BluetoothDevice(
    val deviceId: String,
    val deviceName: String,
    val address: String,
    val isConnectable: Boolean = true
)