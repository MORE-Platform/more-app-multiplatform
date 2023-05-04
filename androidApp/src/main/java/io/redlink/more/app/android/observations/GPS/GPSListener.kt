package io.redlink.more.app.android.observations.GPS

import com.google.android.gms.location.LocationResult

interface GPSListener {
    fun onLocationResult(result: LocationResult)

    fun locationAvailable(available: Boolean)
}