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
import io.redlink.more.more_app_mutliplatform.database.AppDatabase
import io.redlink.more.more_app_mutliplatform.database.entities.ScheduleEntity
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthKitType_HR
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.ObservationType
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDeviceManager
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transform
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ScheduleRepository(private val appDatabase: AppDatabase) {

    fun count() = appDatabase.scheduleDao().countAsFlow()

    fun allSchedulesWithStatus(done: Boolean = false): Flow<List<ScheduleEntity>> {
        return appDatabase.scheduleDao().getByDoneFlow(done)
    }

    fun allScheduleWithRunningState(scheduleState: ScheduleState = ScheduleState.RUNNING): Flow<List<ScheduleEntity>> =
        appDatabase.scheduleDao().getByStateFlow(scheduleState.name)

    fun firstScheduleAvailableForObservationId(observationId: String): Flow<ScheduleEntity?> {
        return appDatabase.scheduleDao().getByObservationIdFlow(observationId)
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

    fun allSchedulesToday(observationType: ObservationType): Flow<List<ScheduleEntity>> {
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

    fun firstScheduleIdAvailableForObservationId(observationId: String): Flow<String?> =
        firstScheduleAvailableForObservationId(observationId).transform { it?.scheduleId }

    fun collectRunningState(
        forState: ScheduleState,
        provideNewState: (List<ScheduleEntity>) -> Unit
    ): Closeable {
        return allScheduleWithRunningState(forState).asClosure(provideNewState)
    }

    fun getFirstAndLastDate(observationId: String): Flow<Pair<ScheduleEntity?, ScheduleEntity?>> {
        return appDatabase.scheduleDao().getByObservationIdFlow(observationId).transform {
            val start = it.sortedBy { it.start }.firstOrNull()
            val end = it.sortedBy { it.end }.lastOrNull()
            emit(Pair(start, end))
        }
    }

    suspend fun setRunningStateFor(id: String, scheduleState: ScheduleState) {
        appDatabase.scheduleDao().updateState(id, scheduleState.name)
    }

    suspend fun setCompletionStateFor(id: String, wasDone: Boolean) {
        val newState = if (wasDone) ScheduleState.DONE else ScheduleState.ENDED
        appDatabase.scheduleDao().updateState(id, newState.name)
        appDatabase.scheduleDao().updateDoneStatus(id, wasDone)
    }

    fun nextSchedule(): Flow<Long?> {
        return allSchedulesWithStatus().transform { schedules ->
            val now = Clock.System.now().epochSeconds
            val startTimes = schedules.mapNotNull { it.start }.filter { it > now }.toSet()
            val endTimes = schedules.mapNotNull { it.end }.filter { it > now }.toSet()
            val nextStart = startTimes.minOfOrNull { it }
            val nextEnd = endTimes.minOfOrNull { it }

            if (nextStart != null && nextEnd != null) {
                if (nextStart < nextEnd) emit(nextStart) else emit(nextEnd)
                return@transform
            }
            if (nextStart != null) emit(nextStart) else emit(nextEnd)
        }
    }

    suspend fun getPreviousSchedule(
        observationId: String,
        currScheduleId: String
    ): ScheduleEntity? {
        val schedules = appDatabase.scheduleDao().getByObservationId(observationId)
        val currentIndex = schedules.indexOfFirst { it.scheduleId == currScheduleId }

        return if (currentIndex > 0) schedules[currentIndex - 1] else null
    }

    fun queryAllSchedulesForObservationId(observationId: String): Flow<List<ScheduleEntity>> {
        return appDatabase.scheduleDao().getByObservationIdFlow(observationId)
    }

    suspend fun getNextSchedule() = nextSchedule().firstOrNull()

    fun nextScheduleStart(): Flow<Long?> {
        return allSchedulesWithStatus().transform { schedules ->
            val now = Clock.System.now().epochSeconds
            val nextStart = schedules.mapNotNull { it.start }
                .filter { it > now }
                .minOfOrNull { it }
            emit(nextStart)
        }
    }

    fun collectNextScheduleStart(provideNewState: (Long?) -> Unit) =
        nextScheduleStart().asClosure(provideNewState)

    fun scheduleWithId(id: String): Flow<ScheduleEntity?> {
        return appDatabase.scheduleDao().getById(id)
    }

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

        try {
            val schedules = appDatabase.scheduleDao().getByDone(false)

            val stateUpdates = mutableListOf<Pair<String, ScheduleState>>()
            val activeIds = mutableSetOf<String>()

            schedules.forEach { scheduleEntity ->
                val newState = scheduleEntity.updateState()

                if (scheduleEntity.getState() != newState) {
                    stateUpdates.add(scheduleEntity.scheduleId to newState)
                    Napier.i { "State update for Entity: $scheduleEntity; ${scheduleEntity.getState()} -> $newState" }
                }

                if (newState == ScheduleState.RUNNING
                    || (autoStartingObservations.isNotEmpty()
                            && scheduleEntity.hidden
                            && newState.active()
                            && scheduleEntity.observationType in autoStartingObservations
                            && observationFactory.observation(scheduleEntity.observationType)
                        ?.bleDevicesNeeded()
                        ?.let { needed ->
                            BluetoothDeviceManager.connectedDevices.value.map { it.deviceName }
                                .containsAll(needed)
                        } != false)
                ) {
                    activeIds.add(scheduleEntity.scheduleId)
                }
            }

            StudyScope.launch(Dispatchers.IO) {
                stateUpdates.forEach { (scheduleId, newState) ->
                    appDatabase.scheduleDao().updateState(scheduleId, newState.name)
                }
            }

            if (activeIds.isNotEmpty()) {
                dataRecorder.startMultiple(activeIds)
            }
        } catch (e: Exception) {
            Napier.e("Error updating schedule states", e)
        }
    }

    suspend fun updateTaskStatesWithBLEDevices(
        observationFactory: ObservationFactory,
        dataRecorder: DataRecorder
    ) {
        val autoStartingObservations = observationFactory.autoStartableObservations()
        if (autoStartingObservations.isNotEmpty()) {
            Napier.i { "Updating Schedule states using Bluetooth devices..." }

            try {
                val schedules = appDatabase.scheduleDao().getByDone(false)

                val activeIds = schedules.filter { scheduleEntity ->
                    observationFactory.observation(scheduleEntity.observationType)
                        ?.bleDevicesNeeded()
                        ?.isNotEmpty() == true && scheduleEntity.observationType in autoStartingObservations
                }.mapNotNull { scheduleEntity ->
                    val newState = scheduleEntity.updateState()

                    if (newState.active() && scheduleEntity.hidden && observationFactory.observation(
                            scheduleEntity.observationType
                        )?.bleDevicesNeeded()
                            ?.let { needed ->
                                BluetoothDeviceManager.connectedDevices.value.map { it.deviceName }
                                    .containsAll(needed)
                            } != false
                    ) {
                        scheduleEntity.scheduleId
                    } else {
                        null
                    }
                }.toSet()

                if (activeIds.isNotEmpty()) {
                    dataRecorder.startMultiple(activeIds)
                }
            } catch (e: Exception) {
                Napier.e("Error updating schedule states with BLE devices", e)
            }
        }
    }
}