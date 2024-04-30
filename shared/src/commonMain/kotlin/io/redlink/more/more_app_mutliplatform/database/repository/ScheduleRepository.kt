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

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.Closeable
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.asMappedFlow
import io.redlink.more.more_app_mutliplatform.extensions.firstAsFlow
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transform
import kotlinx.datetime.Clock
import org.mongodb.kbson.ObjectId

class ScheduleRepository : Repository<ScheduleSchema>() {

    override fun count(): Flow<Long> = realmDatabase().count<ScheduleSchema>()

    fun allSchedulesWithStatus(done: Boolean = false): Flow<List<ScheduleSchema>> {
        return realm()?.query<ScheduleSchema>("done = $0", done)?.asMappedFlow() ?: emptyFlow()
    }

    fun allScheduleWithRunningState(scheduleState: ScheduleState = ScheduleState.RUNNING) =
        realmDatabase().query<ScheduleSchema>(
            query = "state = $0",
            queryArgs = arrayOf(scheduleState.name)
        )

    fun firstScheduleAvailableForObservationId(observationId: String): Flow<ScheduleSchema?> {
        return realm()?.query<ScheduleSchema>("observationId = $0", observationId)?.asMappedFlow()
            ?.transform { scheduleList ->
                if (realm()?.query<ObservationSchema>("observationId = $0", observationId)
                        ?.firstAsFlow()?.firstOrNull()?.scheduleLess == true
                ) {
                    emit(scheduleList.sortedBy { it.end }.last())
                } else {
                    val now = Clock.System.now().epochSeconds
                    val filtered = scheduleList.filter {
                        !it.getState().completed()
                                && it.start != null
                                && it.end != null
                                && (it.end?.epochSeconds ?: 0) > now
                    }.sortedBy { it.start?.epochSeconds }.firstOrNull()
                    emit(filtered)
                }
            } ?: emptyFlow()
    }


    fun firstScheduleIdAvailableForObservationId(observationId: String): Flow<String?> =
        firstScheduleAvailableForObservationId(observationId).transform { it?.scheduleId?.toHexString() }

    fun collectRunningState(
        forState: ScheduleState,
        provideNewState: (List<ScheduleSchema>) -> Unit
    ): Closeable {
        return allScheduleWithRunningState(forState).asClosure(provideNewState)
    }

    fun getFirstAndLastDate(observationId: String): Flow<Pair<ScheduleSchema?, ScheduleSchema?>> {
        return realmDatabase().query<ScheduleSchema>(
            query = "observationId = $0",
            queryArgs = arrayOf(observationId)
        ).transform {
            val start = it.sortedBy { it.start }.firstOrNull()
            val end = it.sortedBy { it.end }.lastOrNull()

            emit(Pair(start, end))
        }
    }

    fun setRunningStateFor(id: String, scheduleState: ScheduleState) {
        realm()?.writeBlocking {
            this.query<ScheduleSchema>("scheduleId = $0", ObjectId(id)).first().find()?.let {
                it.state = scheduleState.name
            }
        }
    }

    fun setCompletionStateFor(id: String, wasDone: Boolean) {
        realm()?.writeBlocking {
            this.query<ScheduleSchema>("scheduleId = $0", ObjectId(id)).first().find()
                ?.updateState(if (wasDone) ScheduleState.DONE else ScheduleState.ENDED)
        }
    }

    fun nextSchedule(): Flow<Long?> {
        return allSchedulesWithStatus().transform { schemas ->
            val startInstances =
                schemas.mapNotNull { it.start }.filter { it > RealmInstant.now() }.toSet()
            val endInstances =
                schemas.mapNotNull { it.end }.filter { it > RealmInstant.now() }.toSet()
            val nextStart = startInstances.minOfOrNull { it.epochSeconds }
            val nextEnd = endInstances.minOfOrNull { it.epochSeconds }
            if (nextStart != null && nextEnd != null) {
                if (nextStart < nextEnd) emit(nextStart) else emit(nextEnd)
                return@transform
            }
            if (nextStart != null) emit(nextStart) else emit(nextEnd)
        }
    }

    suspend fun getNextSchedule() = nextSchedule().firstOrNull()

    fun nextScheduleStart(): Flow<Long?> {
        return allSchedulesWithStatus().transform { schemas ->
            emit(
                schemas.mapNotNull { it.start }.filter { it > RealmInstant.now() }.toSet()
                    .minOfOrNull { it.epochSeconds })
        }
    }

    fun collectNextScheduleStart(provideNewState: (Long?) -> Unit) =
        nextScheduleStart().asClosure(provideNewState)

    fun scheduleWithId(id: String) = realmDatabase().queryFirst<ScheduleSchema>(
        query = "scheduleId = $0",
        queryArgs = arrayOf(ObjectId(id))
    )

    fun updateTaskStates(observationFactory: ObservationFactory, dataRecorder: DataRecorder) {
        StudyScope.launch {
            updateTaskStatesSync(observationFactory, dataRecorder)
        }
    }

    suspend fun updateTaskStatesSync(
        observationFactory: ObservationFactory,
        dataRecorder: DataRecorder
    ) {
        val autoStartingObservations = observationFactory.autoStartableObservations()
        Napier.i { "Updating Schedule states..." }
        val activeIds = realm()?.let {
            it.write {
                query<ScheduleSchema>("done = $0", false).find().mapNotNull { scheduleSchema ->
                    val newState = scheduleSchema.updateState()
                    if (scheduleSchema.getState() != newState) {
                        Napier.i { "State update for Schema: $scheduleSchema; ${scheduleSchema.getState()} -> $newState" }
                    }
                    if (autoStartingObservations.isNotEmpty()
                        && scheduleSchema.hidden
                        && newState.active()
                        && scheduleSchema.observationType in autoStartingObservations
                    ) {
                        scheduleSchema.scheduleId.toHexString()
                    } else {
                        null
                    }
                }
            }
        }?.toSet() ?: emptySet()
        if (activeIds.isNotEmpty()) {
            StudyScope.launch {
                observationFactory.updateObservationErrors()
            }
            dataRecorder.startMultiple(activeIds)
        }
    }


}