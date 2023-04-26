package io.redlink.more.more_app_mutliplatform.database

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.schemas.*
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice

object DatabaseManager: Closeable {
    val database = RealmDatabase
    init {
        database.open(
            setOf(
                StudySchema::class,
                ObservationSchema::class,
                ScheduleSchema::class,
                ObservationDataSchema::class,
                DataPointCountSchema::class,
                NotificationSchema::class,
                BluetoothDevice::class
            )
        )
    }

    fun deleteAll() {
        database.deleteAll()
    }

    override fun close() {
        database.close()
    }
}