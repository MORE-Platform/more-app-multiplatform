package io.redlink.more.more_app_mutliplatform.android.observations.GPS

import android.Manifest
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationResult
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationTypes.GPSType
import java.sql.Date
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
    private var active = false
    var settings = mapOf<String, Any>()

//    fun getPermission(): Set<String> = FactoryManager.getPermissionsForFactory(observationType)

    fun setup(): Boolean {
        return true
    }

    fun activate(): Boolean{
        if (hasPermissions()){
            setup()
            active = true
            return true
        }
        Napier.i(tag = TAG) {"Missing Permission"}
        deactivate()
        return false
    }
    fun deactivate() {
        gpsService.unregisterForLocationUpdates(this)
    }

    override fun start(observationId: String): Boolean {
        if (this.activate()) {
            gpsService.registerForLocationUpdates(this)
            return true
        }
        return false
    }

    override fun stop() {
        this.deactivate()
        gpsService.unregisterForLocationUpdates(this)
    }

    override fun observerAccessible(): Boolean {
        return true
    }

    fun parseValues(data: Location): Any = object {
        val longitude = data.longitude
        val latitude = data.latitude
        val altitude = data.altitude
        val locationProvide = data.provider
    }

    override fun onLocationResult(result: LocationResult) {
        result.locations.forEach { location ->
            val dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(location.time),
                TimeZone.getDefault().toZoneId()
            )
            Log.i(
                TAG,
                "Location to store at time ${dateTime}: LONG: ${location.longitude}, LAT: ${location.latitude}"
            )
            storeData(mapOf("loc" to location, "time" to Date(location.time)))
        }
    }

    private fun hasPermissions(): Boolean {
//        getPermission().forEach{permission ->
//            if (ActivityCompat.checkSelfPermission(context,
//                    permission) == PackageManager.PERMISSION_DENIED
//            ) {
//                return false
//            }
//        }
        return true
    }

    companion object {
        const val LOCATION_INTERVAL_MILLIS_KEY = "location_interval_millis"
    }
}