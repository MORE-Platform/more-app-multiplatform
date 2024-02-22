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
package io.redlink.more.more_app_mutliplatform.viewModels.startupConnection

import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.bluetoothConnection.CoreBluetoothConnectionViewModel

class CoreBLESetupViewModel(observationFactory: ObservationFactory, val coreBluetooth: CoreBluetoothConnectionViewModel): CoreViewModel() {
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