package io.redlink.more.app.android.services.sensorsListener

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object BluetoothStateListener {
    private val _bluetoothEnabled = MutableStateFlow(false)
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled

    var listenerActive = false
        private set

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED == intent?.action) {
                updateBluetoothState(context)
            }
        }
    }

    fun startListening(context: Context) {
        if (!listenerActive) {
            listenerActive = true
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            context.registerReceiver(receiver, filter)
            updateBluetoothState(context)
        }
    }

    fun stopListening(context: Context) {
        if (listenerActive) {
            context.unregisterReceiver(receiver)
            listenerActive = false
        }
    }

    private fun updateBluetoothState(context: Context?) {
        val bluetoothAdapter: BluetoothAdapter? =
            context?.getSystemService(BluetoothAdapter::class.java)
        _bluetoothEnabled.update {
            bluetoothAdapter?.isEnabled ?: false
        }
    }
}