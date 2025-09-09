package io.redlink.more.more_app_mutliplatform.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.redlink.more.more_app_mutliplatform.util.createUUID

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
