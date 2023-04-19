package io.redlink.more.app.android.activities.bluetooth_conntection_view

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.services.bluetooth.AndroidBluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.viewModels.bluetoothConnection.CoreBluetoothConnectionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BluetoothConnectionViewModel(context: Context): ViewModel() {
    private val coreViewModel = CoreBluetoothConnectionViewModel(AndroidBluetoothConnector(context))

    val discoveredDevices = mutableStateListOf<BluetoothDevice>()
    val connectedDevices = mutableStateListOf<BluetoothDevice>()

    val bluetoothIsScanning = mutableStateOf(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.discoveredDevices.collect {
                withContext(Dispatchers.Main) {
                    discoveredDevices.clear()
                    discoveredDevices.addAll(it)
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.connectedDevices.collect {
                withContext(Dispatchers.Main) {
                    connectedDevices.clear()
                    connectedDevices.addAll(it)
                }
            }
        }
        viewModelScope.launch {
            coreViewModel.isScanning.collect {
                bluetoothIsScanning.value = it
            }
        }
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }

    fun scanForDevices() {
        coreViewModel.scanForDevices()
    }

    fun stopScanning() {
        coreViewModel.stopScanning()
    }

    fun connectToDevice(device: BluetoothDevice) {
        coreViewModel.connectToDevice(device)
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        coreViewModel.disconnectFromDevice(device)
    }
}