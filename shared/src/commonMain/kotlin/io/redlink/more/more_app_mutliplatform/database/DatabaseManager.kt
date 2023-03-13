package io.redlink.more.more_app_mutliplatform.database

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema

object DatabaseManager: Closeable {
    val database = RealmDatabase
    init {
        database.open(
            setOf(
                StudySchema::class,
                ObservationSchema::class,
                ScheduleSchema::class,
                ObservationDataSchema::class
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