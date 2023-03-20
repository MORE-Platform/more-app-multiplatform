package io.redlink.more.more_app_mutliplatform.android.observations.GPS

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationResult
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationTypes.GPSType
import java.time.Instant
import io.github.aakira.napier.Napier
import java.time.LocalDateTime
import java.util.*

private const val TAG = "GPSObservation"
private val permissions = setOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
)

class GPSObservation(
    val context: Context,
    private val gpsService: GPSService
) : Observation(observationTypeImpl = GPSType(permissions)), GPSListener {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    fun getPermission(): Set<String> = permissions

    // Implement settingValues on start
    override fun start(observationId: String): Boolean {
        if (this.activate()) {
            gpsService.registerForLocationUpdates(this)
            return true
        }
        return false
    }

    override fun stop() {
        this.gpsService.unregisterForLocationUpdates(this)
    }

    override fun observerAccessible(): Boolean {
        return locationManager != null
                && locationManager.isLocationEnabled
                && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun setObservationConfig(settings: Map<String, Any>) {
        try {
            (settings[LOCATION_INTERVAL_MILLIS_KEY] as? Double)?.toLong()?.let {
                gpsService.setIntervalMillis(it)
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.stackTraceToString())
        }
    }

    override fun onLocationResult(result: LocationResult) {
        result.locations.forEach { location ->
            val dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(location.time),
                TimeZone.getDefault().toZoneId()
            )
            storeData(
                mapOf(
                    "longitude" to location.longitude,
                    "latitude" to location.latitude,
                    "altitude" to location.altitude
                )
            )
        }
    }

    private fun activate(): Boolean {
        if (hasPermissions()) {
            running = true
            return true
        }
        Napier.i(tag = TAG) { "Missing Permission" }
        stop()
        return false
    }

    private fun hasPermissions(): Boolean {
        getPermission().forEach { permission ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }

    companion object {
        const val LOCATION_INTERVAL_MILLIS_KEY = "location_interval_millis"
    }
}