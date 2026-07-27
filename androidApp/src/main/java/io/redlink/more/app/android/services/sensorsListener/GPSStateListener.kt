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

package io.redlink.more.app.android.services.sensorsListener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.content.ContextCompat
import io.redlink.more.scopes.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object GPSStateListener {
    private val _gpsEnabled = MutableStateFlow(false)
    val gpsEnabled: StateFlow<Boolean> = _gpsEnabled
    var listenerActive = false
        private set

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                updateGpsState(context)
            }
        }
    }

    fun startListening(context: Context) {
        if (!listenerActive) {
            listenerActive = true
            Scope.launch(Dispatchers.Main) {
                val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                updateGpsState(context)
            }
        }
    }

    fun stopListening(context: Context) {
        if (listenerActive) {
            context.unregisterReceiver(receiver)
            listenerActive = false
        }
    }

    private fun updateGpsState(context: Context?) {
        val locationManager =
            context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        _gpsEnabled.update {
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        }
    }
}