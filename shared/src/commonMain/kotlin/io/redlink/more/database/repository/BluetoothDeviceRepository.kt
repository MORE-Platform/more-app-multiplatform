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
package io.redlink.more.database.repository

import io.redlink.more.database.AppDatabase
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.scopes.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

open class BluetoothDeviceRepository(
    private val database: AppDatabase
) {
    fun storePairedDevice(bluetoothDevice: BluetoothDeviceEntity) {
        if (bluetoothDevice.address != null) {
            Scope.launch(Dispatchers.IO) {
                database.bluetoothDeviceDao().insert(bluetoothDevice)
            }
        }
    }

    fun unpairDevice(bluetoothDevice: BluetoothDeviceEntity) {
        bluetoothDevice.address?.let {
            Scope.launch(Dispatchers.IO) {
                database.bluetoothDeviceDao().deleteByAddress(it)
            }
        }
    }

    fun pairedDevices(): Flow<List<BluetoothDeviceEntity>> =
        database.bluetoothDeviceDao().getAllFlow()
}