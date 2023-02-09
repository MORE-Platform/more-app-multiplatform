package io.redlink.more.more_app_mutliplatform.services.permissions
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBManagerAuthorizationDenied
import platform.CoreBluetooth.CBManagerAuthorizationNotDetermined
import platform.CoreBluetooth.CBManagerAuthorizationRestricted
import platform.CoreLocation.*

class IOSDevicePermission : DevicePermissions {
    private var locationPermission = false
    private var bluetoothPermission = false
    private var notifications = false

    override fun setLocation() {
        locationPermission = true
    }

    override fun setBluetooth() {
        bluetoothPermission = true
    }

    override fun setNotifications() {
        notifications = true
    }

    override fun requestPermissions(): Boolean {
        if (locationPermission) {
            val locationManager = CLLocationManager()
            val status = CLLocationManager.authorizationStatus()
            if (status == 0) {
                locationManager.requestAlwaysAuthorization()
            } else if (status == 1 || status == 2) {
                return false
            }
        }
        else if (bluetoothPermission) {
            val bluetoothStatus = CBCentralManager.authorization
            if (bluetoothStatus == CBManagerAuthorizationNotDetermined) {
                CBCentralManager()
            } else if (bluetoothStatus == CBManagerAuthorizationDenied || bluetoothStatus == CBManagerAuthorizationRestricted) {
                return false
            }
        }

        return true
    }

}

actual fun getDevicePermissions(): DevicePermissions = IOSDevicePermission()