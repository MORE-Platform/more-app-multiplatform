/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.app.android.activities.bluetooth

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.more.app.android.services.sensorsListener.GPSStateListener
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.dialog.AlertController
import io.redlink.more.dialog.AlertDialogModel
import io.redlink.more.scopes.Scope
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import io.redlink.more.viewModels.ViewManager
import io.redlink.more.viewModels.startupConnection.CoreBluetoothViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BluetoothViewModel : ViewModel() {
    val coreViewModel = CoreBluetoothViewModel(
        MoreApplication.shared!!.observationFactory,
        MoreApplication.shared!!.bluetoothController
    )
    val discoveredDevices = mutableStateListOf<BluetoothDeviceEntity>()
    val connectedDevices = mutableStateListOf<BluetoothDeviceEntity>()
    val connectingDevices = mutableStateListOf<String>()
    val isScanning = mutableStateOf(false)
    val bluetoothPowerState = mutableStateOf(BluetoothStateListener.bluetoothEnabled.value)

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
            BluetoothStateManagement.discoveredDevices.collect {
                withContext(Dispatchers.Main) {
                    discoveredDevices.clear()
                    discoveredDevices.addAll(it)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            BluetoothStateManagement.connectedDevices.collect {
                withContext(Dispatchers.Main) {
                    connectedDevices.clear()
                    connectedDevices.addAll(it)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            BluetoothStateManagement.scanning.collect {
                withContext(Dispatchers.Main) {
                    isScanning.value = it
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.devicesNeededToConnectTo.collect {
                withContext(Dispatchers.Main) {
                    neededDevices.clear()
                    neededDevices.addAll(MoreApplication.shared!!.observationFactory.bleDevicesNeeded())
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            BluetoothStateListener.bluetoothEnabled.collect {
                withContext(Dispatchers.Main) {
                    bluetoothPowerState.value = it
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            BluetoothStateManagement.devicesCurrentlyConnecting.collect {
                withContext(Dispatchers.Main) {
                    connectingDevices.clear()
                    connectingDevices.addAll(it.mapNotNull { it.address })
                }
            }
        }
    }

    fun viewDidAppear() {
        ViewManager.bleViewOpen(true)
        coreViewModel.viewOpened()
    }

    fun viewDidDisappear() {
        ViewManager.bleViewOpen(false)
        coreViewModel.viewClosed()
    }

    fun connectToDevice(device: BluetoothDeviceEntity) {
        Scope.launch {
            coreViewModel.connectToDevice(device)
        }
    }

    fun disconnectFromDevice(device: BluetoothDeviceEntity) {
        coreViewModel.disconnectFromDevice(device)
    }
}