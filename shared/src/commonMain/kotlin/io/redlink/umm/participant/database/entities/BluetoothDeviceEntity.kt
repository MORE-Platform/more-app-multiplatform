package io.redlink.umm.participant.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.redlink.umm.participant.util.createUUID

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
