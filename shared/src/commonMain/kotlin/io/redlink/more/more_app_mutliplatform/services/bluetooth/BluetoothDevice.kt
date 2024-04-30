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
package io.redlink.more.more_app_mutliplatform.services.bluetooth

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class BluetoothDevice : RealmObject {
    @PrimaryKey
    var deviceId: String? = null
    var deviceName: String? = null
    var address: String? = null
    override fun toString(): String {
        return "BluetoothDevice {deviceId: $deviceId, name: $deviceName, address: $address}"
    }

    override fun hashCode(): Int = address?.hashCode() ?: super.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BluetoothDevice

        return this.address == other.address
    }

    companion object {
        fun create(
            deviceId: String,
            deviceName: String,
            address: String,
        ): BluetoothDevice {
            return BluetoothDevice().apply {
                this.deviceId = deviceId
                this.deviceName = deviceName
                this.address = address
            }
        }
    }
}