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

import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDeviceManager
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable

class BluetoothDeviceRepository :
    Repository<BluetoothDevice>() {

    private val deviceManager = BluetoothDeviceManager

    init {
        Scope.launch {
            pairedDevices().cancellable().collect {
                deviceManager.addPairedDeviceIds(it.toSet())
            }
        }
    }

    override fun count(): Flow<Long> = realmDatabase().count<BluetoothDevice>()

    fun storePairedDevice(bluetoothDevice: BluetoothDevice) {
        if (bluetoothDevice.address != null) {
            StudyScope.launch {
                realm()?.write {
                    val device =
                        this.query<BluetoothDevice>("address = $0", bluetoothDevice.address).first()
                            .find()
                    if (device == null) {
                        this.copyToRealm(bluetoothDevice, updatePolicy = UpdatePolicy.ALL)
                    }
                }
            }
        }
    }

    fun unpairDevice(bluetoothDevice: BluetoothDevice) {
        if (bluetoothDevice.address != null) {
            StudyScope.launch {
                realm()?.write {
                    val device =
                        this.query<BluetoothDevice>("address = $0", bluetoothDevice.address).first()
                            .find()
                    if (device != null) {
                        this.delete(device)
                    }
                }
            }
        }
    }

    fun pairedDevices() = realmDatabase().query<BluetoothDevice>()
}