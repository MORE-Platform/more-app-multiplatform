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
import io.redlink.more.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.more.app.android.services.sensorsListener.GPSStateListener
import io.redlink.more.more_app_mutliplatform.AlertController
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDeviceManager
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
            BluetoothDeviceManager.discoveredDevices.collect {
                withContext(Dispatchers.Main) {
                    discoveredDevices.clear()
                    discoveredDevices.addAll(it)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            BluetoothDeviceManager.connectedDevices.collect {
                withContext(Dispatchers.Main) {
                    connectedDevices.clear()
                    connectedDevices.addAll(it)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            coreBluetoothViewModel.coreBluetooth.isScanning.collect {
                withContext(Dispatchers.Main) {
                    isScanning.value = it
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            coreBluetoothViewModel.devicesNeededToConnectTo.collect {
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
            BluetoothDeviceManager.devicesCurrentlyConnecting.collect {
                withContext(Dispatchers.Main) {
                    connectingDevices.clear()
                    connectingDevices.addAll(it.mapNotNull { it.address })
                }
            }
        }
    }

    fun viewDidAppear() {
        ViewManager.bleViewOpen(true)
        coreBluetoothViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreBluetoothViewModel.viewDidDisappear()
        ViewManager.bleViewOpen(false)
    }

    fun connectToDevice(device: BluetoothDevice) {
        coreBluetoothViewModel.connectToDevice(device)
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        coreBluetoothViewModel.disconnectFromDevice(device)
    }
}