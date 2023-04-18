package io.redlink.more.more_app_mutliplatform.android.observations.GPS

import com.google.android.gms.location.LocationResult

interface GPSListener {
    fun onLocationResult(result: LocationResult)
}