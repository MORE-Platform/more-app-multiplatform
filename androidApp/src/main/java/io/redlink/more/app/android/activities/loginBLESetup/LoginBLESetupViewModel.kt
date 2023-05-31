package io.redlink.more.app.android.activities.loginBLESetup

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.viewModels.startupConnection.CoreLoginBLESetupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginBLESetupViewModel(observationFactory: ObservationFactory, bluetoothConnector: BluetoothConnector): ViewModel() {
    private val coreLoginBLESetupViewModel = CoreLoginBLESetupViewModel(observationFactory, bluetoothConnector)
    val discoveredDevices = mutableStateListOf<BluetoothDevice>()
    val connectedDevices = mutableStateListOf<BluetoothDevice>()
    val isScanning = mutableStateOf(false)

    val neededDevices = mutableStateListOf<String>()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            coreLoginBLESetupViewModel.coreBluetooth.discoveredDevices.collect {
                discoveredDevices.clear()
                discoveredDevices.addAll(it)
            }
        }

        viewModelScope.launch {
            coreLoginBLESetupViewModel.coreBluetooth.connectedDevices.collect {
                connectedDevices.clear()
                connectedDevices.addAll(it)
            }
        }

        viewModelScope.launch {
            coreLoginBLESetupViewModel.coreBluetooth.isScanning.collect {
                isScanning.value = it
            }
        }

        viewModelScope.launch {
            coreLoginBLESetupViewModel.devicesNeededToConnectTo.collect {
                neededDevices.clear()
                neededDevices.addAll(it)
            }
        }
    }

    fun viewDidAppear() {
        coreLoginBLESetupViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreLoginBLESetupViewModel.viewDidDisappear()
    }

    fun connectToDevice(device: BluetoothDevice) {
        coreLoginBLESetupViewModel.connectToDevice(device)
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        coreLoginBLESetupViewModel.disconnectFromDevice(device)
    }
}