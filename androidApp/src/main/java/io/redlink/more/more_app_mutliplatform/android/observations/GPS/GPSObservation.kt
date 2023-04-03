package io.redlink.more.more_app_mutliplatform.android.observations.GPS

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationResult
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.GPSType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "GPSObservation"
private val permissions = setOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
)

class GPSObservation(
    context: Context,
    private val gpsService: GPSService
) : Observation(observationType = GPSType(permissions)), GPSListener {
    private val locationManager = context.getSystemService(LocationManager::class.java)
    private val hasPermission = this.hasPermissions(context)
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    fun getPermission(): Set<String> = permissions

    override fun start(): Boolean {
        if (this.activate()) {
            val listener = this
            scope.launch {
                gpsService.registerForLocationUpdates(listener)
            }
            return true
        }
        return false
    }

    override fun stop(onCompletion: () -> Unit) {
        this.gpsService.unregisterForLocationUpdates(this)
        onCompletion()
    }

    override fun observerAccessible(): Boolean {
        return locationManager != null
                && locationManager.isLocationEnabled
                && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
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
        return this.hasPermission
    }

    private fun hasPermissions(context: Context): Boolean  {
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