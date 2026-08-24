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
package io.redlink.more.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.redlink.more.util.createUUID

@Entity
data class BluetoothDeviceEntity(
    @PrimaryKey
    val deviceId: String = createUUID(),
    val deviceName: String? = null,
    val address: String? = null,
) {
    override fun toString(): String {
        return "BluetoothDevice {deviceId: $deviceId, name: $deviceName, address: $address}"
    }

    companion object {
        fun create(
            deviceId: String,
            deviceName: String,
            address: String,
        ): BluetoothDeviceEntity {
            return BluetoothDeviceEntity(deviceId, deviceName, address)
        }
    }
}
