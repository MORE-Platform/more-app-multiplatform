package io.redlink.more.app.android.activities.BLESetup

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import io.redlink.more.more_app_mutliplatform.viewModels.startupConnection.CoreBLESetupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BLESetupViewModel(observationFactory: ObservationFactory, bluetoothConnector: BluetoothConnector): ViewModel() {
    private val coreBLESetupViewModel = CoreBLESetupViewModel(observationFactory, bluetoothConnector)
    val discoveredDevices = mutableStateListOf<BluetoothDevice>()
    val connectedDevices = mutableStateListOf<BluetoothDevice>()
    val isScanning = mutableStateOf(false)
    val bluetoothPowerState = mutableStateOf(false)

    val neededDevices = mutableStateListOf<String>()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            coreBLESetupViewModel.coreBluetooth.discoveredDevices.collect {
                discoveredDevices.clear()
                discoveredDevices.addAll(it)
            }
        }

        viewModelScope.launch {
            coreBLESetupViewModel.coreBluetooth.connectedDevices.collect {
                connectedDevices.clear()
                connectedDevices.addAll(it)
            }
        }

        viewModelScope.launch {
            coreBLESetupViewModel.coreBluetooth.isScanning.collect {
                isScanning.value = it
            }
        }

        viewModelScope.launch {
            coreBLESetupViewModel.devicesNeededToConnectTo.collect {
                neededDevices.clear()
                neededDevices.addAll(it)
            }
        }
        viewModelScope.launch {
            coreBLESetupViewModel.coreBluetooth.bluetoothPower.collect {
                bluetoothPowerState.value = it == BluetoothState.ON
            }
        }
    }

    fun viewDidAppear() {
        coreBLESetupViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreBLESetupViewModel.viewDidDisappear()
    }

    fun connectToDevice(device: BluetoothDevice) {
        coreBLESetupViewModel.connectToDevice(device)
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        coreBLESetupViewModel.disconnectFromDevice(device)
    }
}