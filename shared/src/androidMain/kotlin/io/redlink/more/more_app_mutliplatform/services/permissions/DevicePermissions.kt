package io.redlink.more.more_app_mutliplatform.services.permissions

import android.Manifest
import android.os.Build

class AndroidDevicePermissions(): DevicePermissions {
    private val permissionSet = mutableSetOf<String>()
    override fun setLocation() {
        permissionSet.addAll(
            setOf( Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION))
    }

    override fun setBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionSet.addAll(
                setOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            permissionSet.addAll(
                setOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            )
        }
    }

    override fun setNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionSet.add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun requestPermissions(): Boolean {
       return true
    }

}

actual fun getDevicePermissions(): DevicePermissions = AndroidDevicePermissions()