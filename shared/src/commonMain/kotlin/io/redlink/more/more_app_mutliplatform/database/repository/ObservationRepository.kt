package io.redlink.more.more_app_mutliplatform.database.repository

import io.ktor.utils.io.core.*
import io.realm.kotlin.ext.query
import io.realm.kotlin.internal.platform.freeze
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationSchedule
import kotlinx.coroutines.flow.*

class ObservationRepository : Repository<ObservationSchema>() {
    override fun count(): Flow<Long> = realmDatabase().count<ObservationSchema>()

    fun observations() = realmDatabase().query<ObservationSchema>()

    fun observationWithUndoneSchedules(): Flow<Map<ObservationSchema, List<ScheduleSchema>>> {
        return ScheduleRepository().allSchedulesWithStatus().combine(observations()){ schedules, observations ->
            observations.associateWith { observation -> schedules.filter { it.observationId == observation.observationId } }
        }
    }

    fun lastCollection(type: String, timestamp: Long) {
        realm()?.writeBlocking {
            this.query<ObservationSchema>("observationType == $0", type)
                .find()
                .forEach {
                    it.collectionTimestamp = RealmInstant.from(timestamp, 0)
                }
        }
    }

    fun collectionTimestamp(type: String) =
        realmDatabase().query<ObservationSchema>("observationType == $0", queryArgs = arrayOf(type))
            .transform {
                emit(it
                    .map { observationSchema -> observationSchema.collectionTimestamp }
                    .firstOrNull()
                )
            }

    fun collectTimestampOfType(type: String, newState: (RealmInstant?) -> Unit): Closeable {
       return collectionTimestamp(type).asClosure(newState)
    }

    fun collectObservationsWithUndoneSchedules(newState: (Map<ObservationSchema, List<ScheduleSchema>>) -> Unit): Closeable {
        return observationWithUndoneSchedules().asClosure(newState)
    }

    fun observationTypes(): Flow<Set<String>> {
        return observations().transform { observationList -> emit(observationList.map { it.observationType }.toSet()) }
    }

    fun observationById(observationId: String) = realmDatabase().queryFirst<ObservationSchema>(
        "observationId == $0",
        queryArgs = arrayOf(observationId)
    )

    suspend fun getObservationByObservationId(observationId: String): ObservationSchema? {
        return realmDatabase().queryFirst<ObservationSchema>(
                "observationId == $0",
                queryArgs = arrayOf(observationId)
            ).firstOrNull()?.freeze()
    }
}