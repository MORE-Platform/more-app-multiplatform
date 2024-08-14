package io.redlink.more.app.android.services.sensorsListener

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Dispatchers
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
            Scope.launch(Dispatchers.Main) {
                val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                context.registerReceiver(receiver, filter)
                updateBluetoothState(context)
            }
        }
    }

    fun stopListening(context: Context) {
        if (listenerActive) {
            context.unregisterReceiver(receiver)
            listenerActive = false
        }
    }

    private fun updateBluetoothState(context: Context?) {
        if (context == null) {
            _bluetoothEnabled.update { false }
            return
        }

        val bluetoothManager = ContextCompat.getSystemService(context, BluetoothManager::class.java)
        if (bluetoothManager == null) {
            _bluetoothEnabled.update { false }
            return
        }

        val bluetoothAdapter = bluetoothManager.adapter
        _bluetoothEnabled.update {
            bluetoothAdapter?.isEnabled ?: false
        }
    }
}