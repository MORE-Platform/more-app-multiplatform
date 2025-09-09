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
package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.dao.BluetoothDeviceDao
import io.redlink.more.more_app_mutliplatform.database.entities.BluetoothDeviceEntity
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDeviceManager
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.map

class BluetoothDeviceRepository(
    private val bluetoothDeviceDao: BluetoothDeviceDao
) {

    private val deviceManager = BluetoothDeviceManager

    init {
        Scope.launch(Dispatchers.IO) {
            pairedDevices().cancellable().collect {
                deviceManager.addPairedDeviceIds(it.toSet())
            }
        }
    }

    fun count(): Flow<Long> = bluetoothDeviceDao.getAllFlow().map { it.size.toLong() }

    fun storePairedDevice(bluetoothDevice: BluetoothDeviceEntity) {
        if (bluetoothDevice.address != null) {
            StudyScope.launch {
                val existingDevice = bluetoothDeviceDao.getByAddress(bluetoothDevice.address!!)
                if (existingDevice == null) {
                    val entity = bluetoothDevice.toEntity()
                    bluetoothDeviceDao.insert(entity)
                }
            }
        }
    }

    fun unpairDevice(bluetoothDevice: BluetoothDeviceEntity) {
        bluetoothDevice.address?.let {
            StudyScope.launch(Dispatchers.IO) {
                bluetoothDeviceDao.deleteByAddress(it)
            }
        }
    }

    fun pairedDevices(): Flow<List<BluetoothDeviceEntity>> =
        bluetoothDeviceDao.getAllFlow().map { entities ->
            entities.map { it.toBluetoothDevice() }
        }

    private fun BluetoothDeviceEntity.toEntity(): BluetoothDeviceEntity {
        return BluetoothDeviceEntity(
            deviceId = this.deviceId,
            deviceName = this.deviceName,
            address = this.address
        )
    }

    private fun BluetoothDeviceEntity.toBluetoothDevice(): BluetoothDeviceEntity {
        return BluetoothDeviceEntity(
            this@toBluetoothDevice.deviceId,
            this@toBluetoothDevice.deviceName,
            this@toBluetoothDevice.address
        )
    }
}