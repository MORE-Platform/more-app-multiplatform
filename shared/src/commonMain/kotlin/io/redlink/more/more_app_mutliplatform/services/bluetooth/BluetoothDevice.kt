package io.redlink.more.more_app_mutliplatform.services.bluetooth

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class BluetoothDevice: RealmObject {
    @PrimaryKey
    var deviceId: String? = null
    var deviceName: String? = null
    var address: String? = null
    var isConnectable: Boolean = true
    var connected: Boolean = false

    override fun toString(): String {
        return "BluetoothDevice {deviceId: $deviceId, name: $deviceName, address: $address, connected: $connected}"
    }

    companion object {
        fun create(deviceId: String, deviceName: String, address: String, isConnectable: Boolean = true): BluetoothDevice {
            return BluetoothDevice().apply {
                this.deviceId = deviceId
                this.deviceName = deviceName
                this.address = address
                this.isConnectable = isConnectable
            }
        }
    }
}