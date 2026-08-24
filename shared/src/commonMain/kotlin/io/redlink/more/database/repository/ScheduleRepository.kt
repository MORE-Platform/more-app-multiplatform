/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.database.repository

import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.models.ScheduleState
import io.redlink.more.observations.DataRecorder
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.observations.observationTypes.ObservationType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface ScheduleRepository {

    fun count(): Flow<Int>

    fun allSchedulesWithStatus(done: Boolean = false): Flow<List<ScheduleEntity>>

    fun allSchedulesWithStates(states: Set<ScheduleState>): Flow<List<ScheduleEntity>>

    fun getSchedulesWithReminder(
        states: Set<ScheduleState>,
        minTimestamp: Instant,
        maxTimestamp: Instant,
        limit: Int = 100
    ): Flow<List<ScheduleEntity>>

    fun allScheduleWithRunningState(scheduleState: ScheduleState = ScheduleState.RUNNING): Flow<List<ScheduleEntity>>

    fun firstScheduleAvailableForObservationId(observationId: String): Flow<ScheduleEntity?>

    fun allSchedulesToday(observationType: ObservationType): Flow<List<ScheduleEntity>>

    fun firstScheduleIdAvailableForObservationId(observationId: String): Flow<String?>

    fun observationTypesForScheduleIds(scheduleIds: Set<String>): Flow<Set<String>>

    fun getFirstAndLastDate(observationId: String): Flow<Pair<ScheduleEntity?, ScheduleEntity?>>

    suspend fun setRunningStateFor(id: String, scheduleState: ScheduleState)

    suspend fun setCompletionStateFor(id: String, wasDone: Boolean)

    fun scheduleWithId(id: String): Flow<ScheduleEntity?>

    suspend fun updateTaskStates(
        observationFactory: ObservationFactory,
        dataRecorder: DataRecorder
    )
}