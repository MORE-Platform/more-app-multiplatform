package io.redlink.more.more_app_mutliplatform.services.permissions

interface DevicePermissions {
    fun setLocation()
    fun setBluetooth()
    fun setNotifications()
    fun requestPermissions(): Boolean
}

expect fun getDevicePermissions(): DevicePermissions