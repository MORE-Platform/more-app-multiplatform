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
    var isConnectable: Boolean = true
    var connected: Boolean = false
    var shouldAutomaticallyReconnect: Boolean = false

    override fun toString(): String {
        return "BluetoothDevice {deviceId: $deviceId, name: $deviceName, address: $address, connected: $connected}"
    }

    override fun hashCode(): Int = address?.hashCode() ?: super.hashCode()

    companion object {
        fun create(
            deviceId: String,
            deviceName: String,
            address: String,
            isConnectable: Boolean = true
        ): BluetoothDevice {
            return BluetoothDevice().apply {
                this.deviceId = deviceId
                this.deviceName = deviceName
                this.address = address
                this.isConnectable = isConnectable
            }
        }
    }
}