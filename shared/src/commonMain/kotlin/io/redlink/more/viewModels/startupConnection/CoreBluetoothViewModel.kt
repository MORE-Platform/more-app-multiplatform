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
package io.redlink.more.viewModels.startupConnection

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.viewModels.CoreViewModel
import io.redlink.more.viewModels.bluetoothConnection.BluetoothController
import kotlinx.coroutines.flow.StateFlow

class CoreBluetoothViewModel(
    observationFactory: ObservationFactory,
    val coreBluetooth: BluetoothController
) : CoreViewModel() {
    @NativeCoroutines
    val devicesNeededToConnectTo: StateFlow<Set<String>> = observationFactory.studyObservationTypes

    override fun viewDidAppear() {
        coreBluetooth.viewDidAppear()
    }

    override fun viewDidDisappear() {
        coreBluetooth.viewDidDisappear()
    }

    suspend fun connectToDevice(device: BluetoothDeviceEntity): Boolean {
        return coreBluetooth.connectToDevice(device)
    }

    fun disconnectFromDevice(device: BluetoothDeviceEntity) {
        coreBluetooth.unpairFromDevice(device)
    }
}