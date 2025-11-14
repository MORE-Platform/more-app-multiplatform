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
package io.redlink.more.more_app_mutliplatform.database.repository

import io.ktor.utils.io.core.Closeable
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transform

class ObservationRepository : Repository<ObservationSchema>() {
    override fun count(): Flow<Long> = realmDatabase().count<ObservationSchema>()

    fun observations() = realmDatabase().query<ObservationSchema>()

    fun observationWithUndoneSchedules(): Flow<Map<ObservationSchema, List<ScheduleSchema>>> {
        return ScheduleRepository().allSchedulesWithStatus()
            .combine(observations()) { schedules, observations ->
                observations.associateWith { observation -> schedules.filter { it.observationId == observation.observationId } }
            }
    }

    fun lastCollection(type: String, timestamp: Long) {
        Scope.launch {
            realm()?.write {
                this.query<ObservationSchema>("observationType == $0", type)
                    .find()
                    .forEach {
                        it.collectionTimestamp = RealmInstant.from(timestamp, 0)
                    }
            }
        }
    }

    fun lastCollection(type: Set<String>, timestamp: Long) {
        realm()?.writeBlocking {
            this.query<ObservationSchema>("observationType IN $0", type).find()
                .forEach { it.collectionTimestamp = RealmInstant.from(timestamp, 0) }
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

    fun collectAllTimestamps() =
        observations().transform { emit(it.associate { it.observationType to it.collectionTimestamp }) }

    fun collectTimestampForObservationIds(observationIds: Set<String>) =
        realmDatabase().queryAllWhereFieldInList<ObservationSchema, String>(
            "observationType",
            observationIds
        ).transform {
            emit(
                it.maxByOrNull { it.collectionTimestamp }?.collectionTimestamp
                    ?: RealmInstant.now()
            )
        }

    fun collectTimestampOfType(type: String, newState: (RealmInstant?) -> Unit): Closeable {
        return collectionTimestamp(type).asClosure(newState)
    }

    fun collectAllTimestamps(newState: (Map<String, Long>) -> Unit): Closeable {
        return collectAllTimestamps().transform { emit(it.mapValues { it.value.epochSeconds }) }
            .asClosure(newState)
    }

    fun collectObservationsWithUndoneSchedules(newState: (Map<ObservationSchema, List<ScheduleSchema>>) -> Unit): Closeable {
        return observationWithUndoneSchedules().asClosure(newState)
    }

    fun observationTypes(): Flow<Set<String>> = observations().transform { observationList ->
        emit(observationList.map { it.observationType }.toSet())
    }

    fun observationById(observationId: String) = realmDatabase().queryFirst<ObservationSchema>(
        "observationId == $0",
        queryArgs = arrayOf(observationId)
    )

    suspend fun getObservationByObservationId(observationId: String): ObservationSchema? {
        return realmDatabase().queryFirst<ObservationSchema>(
            "observationId == $0",
            queryArgs = arrayOf(observationId)
        ).firstOrNull()
    }
}