package io.redlink.more.more_app_mutliplatform.database

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.schemas.*
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice

object DatabaseManager: Closeable {
    val database = RealmDatabase
    private val schemas = setOf(
        StudySchema::class,
        ObservationSchema::class,
        ScheduleSchema::class,
        ObservationDataSchema::class,
        DataPointCountSchema::class,
        NotificationSchema::class,
        BluetoothDevice::class
    )

    fun open() {
        database.open(this.schemas)
    }

    fun deleteAll() {
        open()
        database.deleteAll()
        close()
    }

    override fun close() {
        database.close()
    }
}