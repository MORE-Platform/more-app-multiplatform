package io.redlink.more.more_app_mutliplatform.database

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.realm.kotlin.types.RealmObject
import io.redlink.more.more_app_mutliplatform.database.schemas.*
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import kotlin.reflect.KClass

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
        Napier.i { "Opened Database!" }
    }

    fun open() {
        database.open(this.schemas)
    }

    suspend fun deleteAllFromSchema(classes: Set<KClass<out RealmObject>>) {
        classes.forEach {
            database.deleteAlOfSchema(it)
        }
    }

    fun deleteAll() {
        database.deleteAll()
    }

    override fun close() {
        database.close()
    }
}