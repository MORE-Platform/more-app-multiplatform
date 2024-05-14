package io.redlink.more.app.android.services.sensorsListener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object GPSStateListener {
    private val _gpsEnabled = MutableStateFlow<Boolean>(false)
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
            val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            context.registerReceiver(receiver, filter)
            updateGpsState(context)
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