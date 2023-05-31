package io.redlink.more.more_app_mutliplatform.database

import io.github.aakira.napier.Napier
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

    init {
        open()
        Napier.d { "Opened Database!" }
    }

    fun open() {
        database.open(this.schemas)
    }

    fun deleteAll() {
        database.deleteAll()
    }

    override fun close() {
        database.close()
    }
}