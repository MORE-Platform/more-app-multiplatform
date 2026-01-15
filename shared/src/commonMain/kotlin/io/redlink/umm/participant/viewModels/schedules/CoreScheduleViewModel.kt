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
package io.redlink.umm.participant.viewModels.schedules

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.umm.participant.database.entities.ScheduleEntity
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.extensions.mapState
import io.redlink.umm.participant.extensions.time
import io.redlink.umm.participant.models.DateFilterModel
import io.redlink.umm.participant.models.ScheduleListType
import io.redlink.umm.participant.models.ScheduleModel
import io.redlink.umm.participant.models.ScheduleState
import io.redlink.umm.participant.observations.DataRecorder
import io.redlink.umm.participant.observations.Observation
import io.redlink.umm.participant.observations.ObservationStates
import io.redlink.umm.participant.viewModels.CoreViewModel
import io.redlink.umm.participant.viewModels.dashboard.CoreDashboardFilterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CoreScheduleViewModel(
    private val repos: MainRepository,
    private val dataRecorder: DataRecorder,
    private val scheduleListType: ScheduleListType,
    val coreFilterModel: CoreDashboardFilterViewModel
) : CoreViewModel() {
    private val scheduleStates = mutableSetOf<ScheduleState>()
    private var originalScheduleList = emptySet<ScheduleModel>()

    private val _schedulesByDate = MutableStateFlow<Map<Long, List<ScheduleModel>>>(emptyMap())

    @NativeCoroutines
    val schedulesByDate: StateFlow<Map<Long, List<ScheduleModel>>> = _schedulesByDate

    private val parentJob = SupervisorJob()

    @NativeCoroutines
    val observationErrors: StateFlow<Map<String, Set<String>>> =
        ObservationStates.observationErrors.mapState(
            CoroutineScope(parentJob)
        ) {
            it.mapValues { entry ->
                entry.value.filter { it != Observation.ERROR_DEVICE_NOT_CONNECTED }.toSet()
            }
        }

    @NativeCoroutines
    val numberOfErrors: StateFlow<Int> = observationErrors.mapState(CoroutineScope(parentJob)) {
        it.values.flatten().toSet().count()
    }

    private val sortedSchedulesCache = mutableMapOf<LocalDate, List<ScheduleModel>>()
    private var cacheVersion = 0L

    init {
        scheduleStates.addAll(
            when (scheduleListType) {
                ScheduleListType.MANUALS -> setOf(
                    ScheduleState.DEACTIVATED, ScheduleState.ACTIVE,
                    ScheduleState.RUNNING, ScheduleState.PAUSED
                )
                ScheduleListType.RUNNING -> {
                    setOf(ScheduleState.RUNNING)
                }
                else -> {
                    setOf(ScheduleState.DONE, ScheduleState.ENDED)
                }
            }
        )

        launchScope {
            coreFilterModel.currentTypeFilter
                .combine(coreFilterModel.currentDateFilter) { typeFilter, dateFilter ->
                    typeFilter.any { it.value }
                            || (dateFilter[DateFilterModel.ENTIRE_TIME] == false && dateFilter.any { it.value })
                }
                .cancellable().collect { applyFilter ->
                    if (applyFilter) {
                        updateSchedulesFromSnapshot(
                            coreFilterModel.applyFilter(originalScheduleList).toSet()
                        )
                    } else {
                        updateSchedulesFromSnapshot(originalScheduleList)
                    }
                }
        }

        launchScope {
            repos.schedule.allSchedulesWithStates(scheduleStates)
                .cancellable()
                .collect { schedules ->
                    val newList = when (scheduleListType) {
                        ScheduleListType.COMPLETED -> createCompletedModels(schedules)
                        ScheduleListType.RUNNING -> createRunningModels(schedules)
                        ScheduleListType.MANUALS -> createManualTasks(schedules)
                        else -> createModels(schedules)
                    }

                    originalScheduleList = newList.toSet()

                    val modified = if (coreFilterModel.filterActive()) {
                        coreFilterModel.applyFilter(newList)
                    } else {
                        newList
                    }.toSet()
                    updateSchedulesFromSnapshot(modified)
                }
        }
    }

    fun start(scheduleId: String) {
        dataRecorder.start(scheduleId)
    }

    fun pause(scheduleId: String) {
        dataRecorder.pause(scheduleId)
    }

    fun stop(scheduleId: String) {
        dataRecorder.stop(scheduleId)
    }

    private fun createModels(scheduleList: List<ScheduleEntity>): List<ScheduleModel> {
        return scheduleList
            .mapNotNull { ScheduleModel.createModel(it) }
    }

    private fun createCompletedModels(scheduleList: List<ScheduleEntity>): List<ScheduleModel> {
        return createModels(scheduleList.filter { it.getState().completed() })
    }

    private fun createRunningModels(scheduleList: List<ScheduleEntity>): List<ScheduleModel> {
        return createModels(scheduleList.filter { it.getState().running() })
    }

    private fun createManualTasks(scheduleList: List<ScheduleEntity>): List<ScheduleModel> {
        return createModels(scheduleList.filter { !it.hidden })
    }

    private fun updateSchedulesFromSnapshot(newSchedules: Collection<ScheduleModel>) {
        val newMap: Map<Long, List<ScheduleModel>> =
            newSchedules
                .groupBy { schedule ->
                    Instant.fromEpochSeconds(schedule.start)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                        .time()
                }
                .mapValues { (_, schedules) ->
                    schedules
                        .distinctBy { it.scheduleId }
                        .sortedWith(compareBy<ScheduleModel> { it.start }.thenBy { it.scheduleId })
                }

        if (newMap != _schedulesByDate.value) {
            _schedulesByDate.update { newMap }
            invalidateCache()
        }
    }

    private fun invalidateCache() {
        sortedSchedulesCache.clear()
        cacheVersion++
    }
}

