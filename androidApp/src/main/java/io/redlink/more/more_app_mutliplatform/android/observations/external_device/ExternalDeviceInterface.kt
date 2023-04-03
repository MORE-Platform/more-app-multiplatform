package io.redlink.more.more_app_mutliplatform.android.observations.external_device

interface ExternalDeviceInterface<T> {

    var sensor: T?

    fun searchForDevice(name: String): Boolean

    fun getDeviceWithName(name: String): Boolean

    fun stopScanning()

    fun connectToDevice(device: T): Boolean

    fun disconnectFromDevice(device: T)
}