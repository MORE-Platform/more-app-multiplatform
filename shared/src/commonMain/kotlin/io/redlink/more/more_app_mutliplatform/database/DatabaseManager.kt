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
package io.redlink.more.more_app_mutliplatform.database

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.Closeable
import io.realm.kotlin.types.RealmObject
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
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