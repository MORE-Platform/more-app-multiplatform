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
package io.redlink.more.app.android.activities.BLESetup

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import io.redlink.more.more_app_mutliplatform.viewModels.startupConnection.CoreBLESetupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BLESetupViewModel: ViewModel() {
    private val coreBLESetupViewModel = CoreBLESetupViewModel(MoreApplication.shared!!.observationFactory, MoreApplication.shared!!.coreBluetooth)
    val discoveredDevices = mutableStateListOf<BluetoothDevice>()
    val connectedDevices = mutableStateListOf<BluetoothDevice>()
    val connectingDevices = mutableStateListOf<String>()
    val isScanning = mutableStateOf(false)
    val bluetoothPowerState = mutableStateOf(false)

    val neededDevices = mutableStateListOf<String>()

    val alertDialogOpen = mutableStateOf<AlertDialogModel?>(null)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            MoreApplication.shared!!.mainContentCoreViewModel.alertDialogModel.collect {
                withContext(Dispatchers.Main) {
                    alertDialogOpen.value = it
                }
            }
        }
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
                neededDevices.addAll(MoreApplication.shared!!.observationFactory.bleDevicesNeeded(it))
            }
        }
        viewModelScope.launch {
            coreBLESetupViewModel.coreBluetooth.bluetoothPower.collect {
                bluetoothPowerState.value = it == BluetoothState.ON
            }
        }

        viewModelScope.launch {
            coreBLESetupViewModel.coreBluetooth.connectingDevices.collect {
                connectingDevices.clear()
                connectingDevices.addAll(it)
            }
        }
    }

    fun viewDidAppear() {
        coreBLESetupViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreBLESetupViewModel.viewDidDisappear()
        connectingDevices.clear()
        connectedDevices.clear()
        discoveredDevices.clear()
        isScanning.value = false
        bluetoothPowerState.value = false
    }

    fun connectToDevice(device: BluetoothDevice) {
        coreBLESetupViewModel.connectToDevice(device)
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        coreBLESetupViewModel.disconnectFromDevice(device)
    }
}