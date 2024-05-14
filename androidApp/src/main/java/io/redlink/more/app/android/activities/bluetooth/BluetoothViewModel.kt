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
package io.redlink.more.app.android.activities.bluetooth

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.services.sensorsListener.GPSStateListener
import io.redlink.more.more_app_mutliplatform.AlertController
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDeviceManager
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import io.redlink.more.more_app_mutliplatform.viewModels.ViewManager
import io.redlink.more.more_app_mutliplatform.viewModels.startupConnection.CoreBluetoothViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BluetoothViewModel : ViewModel() {
    private val coreBluetoothViewModel = CoreBluetoothViewModel(
        MoreApplication.shared!!.observationFactory,
        MoreApplication.shared!!.bluetoothController
    )
    val discoveredDevices = mutableStateListOf<BluetoothDevice>()
    val connectedDevices = mutableStateListOf<BluetoothDevice>()
    val connectingDevices = mutableStateListOf<String>()
    val isScanning = mutableStateOf(false)
    val bluetoothPowerState = mutableStateOf(false)

    val neededDevices = mutableStateListOf<String>()

    val alertDialogOpen = mutableStateOf<AlertDialogModel?>(null)
    val gpsActive = mutableStateOf(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            GPSStateListener.gpsEnabled.collect {
                withContext(Dispatchers.Main) {
                    gpsActive.value = it
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            AlertController.alertDialogModel.collect {
                withContext(Dispatchers.Main) {
                    alertDialogOpen.value = it
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            BluetoothDeviceManager.discoveredDevices.collect {
                discoveredDevices.clear()
                discoveredDevices.addAll(it)
            }
        }

        viewModelScope.launch {
            BluetoothDeviceManager.connectedDevices.collect {
                connectedDevices.clear()
                connectedDevices.addAll(it)
            }
        }

        viewModelScope.launch {
            coreBluetoothViewModel.coreBluetooth.isScanning.collect {
                isScanning.value = it
            }
        }

        viewModelScope.launch {
            coreBluetoothViewModel.devicesNeededToConnectTo.collect {
                neededDevices.clear()
                neededDevices.addAll(MoreApplication.shared!!.observationFactory.bleDevicesNeeded())
            }
        }
        viewModelScope.launch {
            coreBluetoothViewModel.coreBluetooth.bluetoothPower.collect {
                bluetoothPowerState.value = it == BluetoothState.ON
            }
        }

        viewModelScope.launch {
            BluetoothDeviceManager.devicesCurrentlyConnecting.collect {
                connectingDevices.clear()
                connectingDevices.addAll(it.mapNotNull { it.address })
            }
        }
    }

    fun viewDidAppear() {
        ViewManager.bleViewOpen(true)
        coreBluetoothViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreBluetoothViewModel.viewDidDisappear()
        isScanning.value = false
        bluetoothPowerState.value = false
        ViewManager.bleViewOpen(false)
    }

    fun connectToDevice(device: BluetoothDevice) {
        coreBluetoothViewModel.connectToDevice(device)
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        coreBluetoothViewModel.disconnectFromDevice(device)
    }
}