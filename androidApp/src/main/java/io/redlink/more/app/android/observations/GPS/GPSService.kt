package io.redlink.more.app.android.observations.GPS

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import io.github.aakira.napier.Napier


private const val TAG = "GPSService"

class GPSService(context: Context) {
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            gpsListener?.onLocationResult(result)
        }

        override fun onLocationAvailability(result: LocationAvailability) {
            super.onLocationAvailability(result)
            gpsListener?.locationAvailable(result.isLocationAvailable)
        }
    }

    private var gpsListener: GPSListener? = null

    init {
        setWaitForAccurateLocation(false)
//        setDurationMillis(1000)
        setMinUpdateIntervalMillis(500)
        setMaxUpdateAgeMillis(1000)
        setGranularity(Granularity.GRANULARITY_FINE)
//        setMinUpdateDistanceMeters(10f)
    }

    fun setPriority(priority: Int) {
        this.locationRequest.setPriority(priority)
    }

    fun setDurationMillis(durationMillis: Long) {
        this.locationRequest.setDurationMillis(durationMillis)
    }

    fun setGranularity(granularity: Int) {
        this.locationRequest.setGranularity(granularity)
    }

    fun setIntervalMillis(intervalMillis: Long) {
        this.locationRequest.setIntervalMillis(intervalMillis)
    }

    fun setMaxUpdateAgeMillis(maxUpdateAgeMillis: Long) {
        this.locationRequest.setMaxUpdateAgeMillis(maxUpdateAgeMillis)
    }

    fun setMaxUpdateDelayMillis(maxUpdateDelayMillis: Long) {
        this.locationRequest.setMaxUpdateDelayMillis(maxUpdateDelayMillis)
    }

    fun setMaxUpdates(maxUpdates: Int) {
        this.locationRequest.setMaxUpdates(maxUpdates)
    }

    fun setMinUpdateDistanceMeters(minUpdateDistanceMeter: Float) {
        this.locationRequest.setMinUpdateDistanceMeters(minUpdateDistanceMeter)
    }

    fun setMinUpdateIntervalMillis(minUpdateIntervalMillis: Long) {
        this.locationRequest.setMinUpdateIntervalMillis(minUpdateIntervalMillis)
    }

    fun setWaitForAccurateLocation(waitForAccurateLocation: Boolean) {
        this.locationRequest.setWaitForAccurateLocation(waitForAccurateLocation)
    }

    @SuppressLint("MissingPermission")
    fun registerForLocationUpdates(listener: GPSListener) {
        Napier.d(tag = "GPSService::registerForLocationUpdates") { "Registered new listener!" }
        this.gpsListener = listener
        fusedLocationProviderClient.requestLocationUpdates(locationRequest.build(), locationCallback, Looper.getMainLooper())
    }

    fun unregisterForLocationUpdates(listener: GPSListener) {
        gpsListener?.let {
            if (it == listener) {
                Napier.d(tag = "GPSService::unregisterForLocationUpdates") { "Unregistered listener!" }
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                this.gpsListener = null
            }
        }
    }

}