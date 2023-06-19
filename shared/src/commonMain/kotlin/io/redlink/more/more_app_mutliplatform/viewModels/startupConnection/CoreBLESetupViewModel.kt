package io.redlink.more.more_app_mutliplatform.viewModels.startupConnection

import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.bluetoothConnection.CoreBluetoothConnectionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull

class CoreBLESetupViewModel(observationFactory: ObservationFactory, bluetoothConnector: BluetoothConnector): CoreViewModel() {
    val coreBluetooth = CoreBluetoothConnectionViewModel(bluetoothConnector, scanInterval = 2000)

    val devicesNeededToConnectTo = observationFactory.studyObservationTypes

    override fun viewDidAppear() {
        coreBluetooth.viewDidAppear()
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        coreBluetooth.viewDidDisappear()
    }

    fun connectToDevice(device: BluetoothDevice): Boolean {
        return coreBluetooth.connectToDevice(device)
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        coreBluetooth.disconnectFromDevice(device)
    }

    fun devicesNeededChange(providedState: (Set<String>) -> Unit) = devicesNeededToConnectTo.asClosure(providedState)
}