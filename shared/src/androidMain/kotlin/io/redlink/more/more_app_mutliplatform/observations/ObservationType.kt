package io.redlink.more.more_app_mutliplatform.observations

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

enum class ObservationType(val observerType: String, val sensorPermission: String? = null) {
    NON(observerType = "non"),
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    NOTIFICATION(observerType = "notifications", Manifest.permission.POST_NOTIFICATIONS),
    CAMERA(observerType = "smartphone_camera", Manifest.permission.CAMERA),
    FINE_LOCATION(observerType = "smartphone_gps_fine", Manifest.permission.ACCESS_FINE_LOCATION),
    COARSE_LOCATION(observerType = "smartphone_gps_coarse", Manifest.permission.ACCESS_COARSE_LOCATION),
    ACCELEROMETER(observerType = "ACCELEROMETER"),
    MICROPHONE(observerType = "smartphone_microphone", Manifest.permission.RECORD_AUDIO),
    GYROSCOPE(observerType = "smartphone_gyroscope"),
    LIGHT(observerType = "smartphone_light"),
    MAGNETOMETER(observerType = "smartphone_magnetometer"),
    @RequiresApi(Build.VERSION_CODES.S)
    BLE_SCAN(observerType = "ble_scan", Manifest.permission.BLUETOOTH_SCAN),
    BT(observerType = "bt", Manifest.permission.BLUETOOTH),
    @RequiresApi(Build.VERSION_CODES.S)
    BLE_CONNECT(observerType = "ble_connect", Manifest.permission.BLUETOOTH_CONNECT),
    BLE_ADMIN(observerType = "ble_admin", Manifest.permission.BLUETOOTH_ADMIN);


    companion object {
        fun contains(observationType: String) = enumValues<ObservationType>().any { it.observerType == observationType}

        fun getObserverType(observationType: String) = enumValues<ObservationType>().find { it.observerType == observationType }

        fun getObserverTypeOrDefault(observationType: String) = getObserverType(observationType) ?: NON

        fun getSensorPermission(observationType: String) = getObserverType(observationType)?.sensorPermission

        fun containsSensorPermission(observationType: String) = getSensorPermission(observationType) != null
    }
}