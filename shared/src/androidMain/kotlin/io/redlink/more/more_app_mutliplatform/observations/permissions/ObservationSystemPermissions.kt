package io.redlink.more.more_app_mutliplatform.observations.permissions

import android.Manifest
import android.os.Build
import io.redlink.more.more_app_mutliplatform.managers.ObservationsManager

enum class ObservationSystemPermissions(val observerType: String, val permissions: Set<String>) {
    GPS_OBSERVER_PERMISSIONS(
        observerType = ObservationsManager.GPS_OBSERVER_TYPE, setOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    ),
    POLAR_VERITY_HR_OBSERVER_PERMISSIONS(
        observerType = ObservationsManager.POLAR_VERITY_HR_OBSERVER_TYPE,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            setOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
    ),
    ACCELEROMETER_OBSERVER_PERMISSIONS(observerType = ObservationsManager.ACCELEROMETER_OBSERVER_TYPE, emptySet()),
    QUESTION_OBSERVER_PERMISSIONS(observerType = ObservationsManager.QUESTION_OBSERVER_TYPE, emptySet())
}