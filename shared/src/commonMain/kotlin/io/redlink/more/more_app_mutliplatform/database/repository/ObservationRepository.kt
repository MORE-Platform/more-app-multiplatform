package io.redlink.more.more_app_mutliplatform.database.repository

import io.ktor.utils.io.core.*
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import kotlinx.coroutines.flow.*

class ObservationRepository(database: RealmDatabase = DatabaseManager.database) : Repository<ObservationSchema>(database) {

    override fun count(): Flow<Long> = realmDatabase.count<ObservationSchema>()

    fun observationWithUndoneSchedules(): Flow<List<ObservationSchema>> {
        return realmDatabase.query(
            query = "schedules.done = false",
            limit = 1000
        )
    }

    fun lastCollection(type: String, timestamp: Long) {
        realmDatabase.realm?.writeBlocking {
            this.query<ObservationSchema>("observationType == $0", type)
                .find()
                .forEach {
                    it.collectionTimestamp = RealmInstant.from(timestamp, 0)
                }
        }
    }

    fun collectionTimestamp(type: String) =
        realmDatabase.query<ObservationSchema>("observationType == $0", queryArgs = arrayOf(type))
            .map { it.map { observationSchema -> observationSchema.collectionTimestamp } }
            .transform { emit(it.firstOrNull()) }

    fun collectTimestampOfType(type: String, newState: (RealmInstant?) -> Unit): Closeable {
       return collectionTimestamp(type).asClosure(newState)
    }

    fun collectObservationsWithUndoneSchedules(newState: (List<ObservationSchema>) -> Unit): Closeable {
        return observationWithUndoneSchedules().asClosure(newState)
    }

    fun observationById(observationId: String) = realmDatabase.queryFirst<ObservationSchema>(
        "observationId == $0",
        queryArgs = arrayOf(observationId)
    )

    suspend fun getObservationByObservationId(observationId: String): ObservationSchema? {
        return realmDatabase.queryFirst<ObservationSchema>(
            "observationId == $0",
            queryArgs = arrayOf(observationId)
        ).firstOrNull()
    }
}