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
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.location.LocationResult
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.services.sensorsListener.GPSStateListener
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.observations.Observation
import io.redlink.more.observations.observationTypes.GPSType
import io.redlink.more.scopes.Scope
import io.redlink.more.services.store.PermissionApprovalState

private const val TAG = "GPSObservation"
private val permissions = setOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
)

class GPSObservation(
    context: Context,
    repos: MainRepository,
    private val gpsService: GPSService
) : Observation(repos, observationType = GPSType(permissions)),
    GPSListener {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    override fun start(): Boolean {
        Napier.d { "Registering GPS Service..." }
        gpsService.registerForLocationUpdates(this)
        return true
    }

    override fun stop(onCompletion: () -> Unit) {
        Napier.d { "Unregistering GPS Service..." }
        this.gpsService.unregisterForLocationUpdates(this)
        onCompletion()
    }

    override fun observerErrors(): Set<String> {
        val errors = mutableSetOf<String>()
        if (locationManager == null) {
            errors.add("error_location_services")
        }
        if (!GPSStateListener.gpsEnabled.value) {
            errors.add("location_disabled")
        }
        if (this.hasPermission() != PermissionApprovalState.GRANTED) {
            errors.add("location_permission_not_granted")
        }
        return errors
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
        try {
            settings[LOCATION_INTERVAL_MILLIS_KEY]?.toString()?.trim('\"')?.toLong()?.let {
                gpsService.setIntervalMillis(it)
            }
        } catch (e: Exception) {
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
        if (!available) {
            Scope.launch() {
                updateObservationErrors()
            }
        }
    }

    companion object {
        const val LOCATION_INTERVAL_MILLIS_KEY = "location_interval_millis"
    }
}
