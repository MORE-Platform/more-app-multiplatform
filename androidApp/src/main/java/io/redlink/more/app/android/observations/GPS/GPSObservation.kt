/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.observations.GPS

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationResult
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.observations.showPermissionAlertDialog
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
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
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun getPermission(): Set<String> = permissions

    override fun start(): Boolean {
        Napier.d { "Trying to start GPS..." }
        if (this.activate()) {
            val listener = this
            scope.launch {
                Napier.d { "Registering GPS Service..." }
                gpsService.registerForLocationUpdates(listener)
            }
            return true
        }
        showPermissionAlertDialog()
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
            settings[LOCATION_INTERVAL_MILLIS_KEY]?.toString()?.trim('\"')?.toLong()?.let {
                //gpsService.setIntervalMillis(it)
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

    override fun locationAvailable(available: Boolean) {
        Napier.d { "Location available: $available" }
    }

    private fun activate(): Boolean {
        return this.hasPermissions(MoreApplication.appContext!!)
    }

    private fun hasPermissions(context: Context): Boolean  {
        getPermission().forEach { permission ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                Napier.d { "Has no GPS permissions!" }
                return false
            }
        }
        Napier.d { "Has GPS permissions!" }
        return true
    }

    companion object {
        const val LOCATION_INTERVAL_MILLIS_KEY = "location_interval_millis"
    }
}