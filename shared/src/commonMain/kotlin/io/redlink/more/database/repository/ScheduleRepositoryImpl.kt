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
package io.redlink.more.database.repository

import io.github.aakira.napier.Napier
import io.redlink.more.database.AppDatabase
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.models.ScheduleState
import io.redlink.more.observations.DataRecorder
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.observations.observationTypes.ObservationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ScheduleRepositoryImpl(private val appDatabase: AppDatabase) : ScheduleRepository {

    private val mutex = Mutex()

    override fun count(): Flow<Int> = appDatabase.scheduleDao().countAsFlow()

    override fun allSchedulesWithStatus(done: Boolean): Flow<List<ScheduleEntity>> {
        return appDatabase.scheduleDao().getByDoneFlow(done)
    }

    override fun allSchedulesWithStates(states: Set<ScheduleState>): Flow<List<ScheduleEntity>> {
        if (states.isEmpty()) {
            return flowOf(emptyList())
        }
        return appDatabase.scheduleDao().getByStatesFlow(states.map { it.name })
    }

    override fun getSchedulesWithReminder(
        states: Set<ScheduleState>,
        minTimestamp: Instant,
        maxTimestamp: Instant,
        limit: Int
    ): Flow<List<ScheduleEntity>> {
        if (states.isEmpty()) {
            return flowOf(emptyList())
        }
        return appDatabase.scheduleDao().getSchedulesWithReminder(
            states.map { it.name },
            minTimestamp.epochSeconds,
            maxTimestamp.epochSeconds,
            limit
        )
    }

    override fun allScheduleWithRunningState(scheduleState: ScheduleState): Flow<List<ScheduleEntity>> =
        appDatabase.scheduleDao().getByStateFlow(scheduleState.name)

    override fun firstScheduleAvailableForObservationId(observationId: String): Flow<ScheduleEntity?> {
        return appDatabase.scheduleDao().getByObservationIdFlow(observationId)
            .distinctUntilChanged()
            .transform { scheduleList ->
                val observation = appDatabase.observationDao().getByObservationId(observationId)
                if (observation?.scheduleLess == true) {
                    emit(scheduleList.sortedBy { it.end }.lastOrNull())
                } else {
                    val now = Clock.System.now().epochSeconds
                    val filtered = scheduleList.filter {
                        !it.getState().completed()
                                && it.start != null
                                && it.end != null
                                && it.end > now
                    }.sortedBy { it.start }.firstOrNull()
                    emit(filtered)
                }
            }
    }

    override fun allSchedulesToday(observationType: ObservationType): Flow<List<ScheduleEntity>> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return appDatabase.scheduleDao().getByObservationTypeFlow(observationType.observationType)
            .transform { list ->
                emit(list.filter {
                    !it.getState().completed()
                            && (it.startInstant()
                        ?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == today
                            || it.endInstant()
                        ?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == today)
                })
            }
    }

    override fun firstScheduleIdAvailableForObservationId(observationId: String): Flow<String?> =
        firstScheduleAvailableForObservationId(observationId).transform { it?.scheduleId }

    override fun observationTypesForScheduleIds(scheduleIds: Set<String>): Flow<Set<String>> {
        return appDatabase.scheduleDao().getObservationTypesForScheduleIds(scheduleIds)
            .map { it.toSet() }
    }

    override fun getFirstAndLastDate(observationId: String): Flow<Pair<ScheduleEntity?, ScheduleEntity?>> {
        return appDatabase.scheduleDao().getByObservationIdFlow(observationId).transform {
            val start = it.sortedBy { it.start }.firstOrNull()
            val end = it.sortedBy { it.end }.lastOrNull()
            emit(Pair(start, end))
        }
    }

    override suspend fun setRunningStateFor(id: String, scheduleState: ScheduleState) {
        appDatabase.scheduleDao().updateState(id, scheduleState.name)
    }

    override suspend fun setCompletionStateFor(id: String, wasDone: Boolean) {
        val newState = if (wasDone) ScheduleState.DONE else ScheduleState.ENDED
        appDatabase.scheduleDao().updateState(id, newState.name)
        appDatabase.scheduleDao().updateDoneStatus(id, wasDone)
    }

    override fun scheduleWithId(id: String): Flow<ScheduleEntity?> {
        return appDatabase.scheduleDao().getById(id)
    }

    override suspend fun updateTaskStates(
        observationFactory: ObservationFactory,
        dataRecorder: DataRecorder
    ) {
        if (mutex.isLocked) {
            return
        }
        mutex.withLock {
            val autoStartingObservations = observationFactory.autoStartableObservations()
            Napier.i { "Updating Schedule states..." }

            try {
                val schedules = appDatabase
                    .scheduleDao()
                    .getByStatesFlow(ScheduleState.presentScheduleStates.map { it.name })
                    .firstOrNull()
                    ?: emptyList()

                val stateUpdates = mutableListOf<Pair<String, ScheduleState>>()
                val activeIds = mutableSetOf<String>()
                val pausingIds = mutableSetOf<String>()

                schedules.forEach { scheduleEntity ->
                    val newState = scheduleEntity.updateState()

                    if (scheduleEntity.getState() != newState) {
                        stateUpdates.add(scheduleEntity.scheduleId to newState)
                        Napier.i { "State update for Entity: $scheduleEntity; ${scheduleEntity.getState()} -> $newState" }
                    }

                    if (newState == ScheduleState.RUNNING
                        || scheduleEntity.hidden
                        && newState.active()
                        && scheduleEntity.observationType in autoStartingObservations
                    ) {
                        observationFactory.observation(scheduleEntity.observationType)
                            ?.let { observation ->
                                if (observation.observerAccessible()) {
                                    activeIds.add(scheduleEntity.scheduleId)
                                } else {
                                    pausingIds.add(scheduleEntity.scheduleId)
                                }
                            }
                    }
                }

                stateUpdates.forEach { (scheduleId, newState) ->
                    Napier.d { "Updating the schedule with $scheduleId to state: $newState" }
                    appDatabase.scheduleDao().updateState(scheduleId, newState.name)
                }

                if (activeIds.isNotEmpty()) {
                    dataRecorder.startMultiple(activeIds)
                }
                if (pausingIds.isNotEmpty()) {
                    pausingIds.forEach { scheduleId ->
                        dataRecorder.pause(scheduleId)
                    }
                }
            } catch (e: Exception) {
                Napier.e("Error updating schedule states", e)
            }
        }

    }
}
