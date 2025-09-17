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
package io.redlink.more.more_app_mutliplatform.viewModels.schedules

import io.redlink.more.more_app_mutliplatform.database.entities.ScheduleEntity
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.extensions.time
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CoreScheduleViewModel(
    private val repos: MainRepository,
    private val dataRecorder: DataRecorder,
    private val scheduleListType: ScheduleListType,
    val coreFilterModel: CoreDashboardFilterViewModel,
    private val observationFactory: ObservationFactory,
) : CoreViewModel() {
    private var originalScheduleList = emptySet<ScheduleModel>()

    private val _schedulesByDate = MutableStateFlow<Map<Long, List<ScheduleModel>>>(emptyMap())
    val schedulesByDate: StateFlow<Map<Long, List<ScheduleModel>>> = _schedulesByDate

    private val _observationErrors = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val observationErrors: StateFlow<Map<String, Set<String>>> = _observationErrors

    private val sortedSchedulesCache = mutableMapOf<LocalDate, List<ScheduleModel>>()
    private var cacheVersion = 0L

    init {
        launchScope {
            observationFactory.observationErrors.collect {
                val actions = it.mapValues { entry ->
                    entry.value.filter { it == Observation.ERROR_DEVICE_NOT_CONNECTED }.toSet()
                }
                val errors = it.mapValues { entry ->
                    entry.value.filter { it != Observation.ERROR_DEVICE_NOT_CONNECTED }.toSet()
                }
                withContext(Dispatchers.Main) {
                    _observationErrors.update { errors }
                }
            }
        }

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
            repos.schedule.allSchedulesWithStatus(done = scheduleListType == ScheduleListType.COMPLETED)
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

    fun numberOfObservationErrors(): Int = _observationErrors.value.values.flatten().toSet().count()

    fun getSortedSchedulesForDate(date: LocalDate): List<ScheduleModel> {
        return sortedSchedulesCache.getOrPut(date) {
            _schedulesByDate.value[date.time()]?.sortedWith(
                compareBy(
                    { it.start },
                    { it.end },
                    { it.observationTitle },
                    { it.scheduleId }
                )
            ) ?: emptyList()
        }
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

    private fun updateSchedulesEfficiently(
        added: Set<ScheduleModel>,
        removed: Set<String>,
        updated: Set<ScheduleModel>
    ) {
        val currentSchedules = _schedulesByDate.value.toMutableMap()

        if (removed.isNotEmpty() || updated.isNotEmpty()) {
            val idsToRemove = removed + updated.map { it.scheduleId }
            val updatedSchedules = mutableMapOf<Long, List<ScheduleModel>>()
            for ((date, schedules) in currentSchedules) {
                val filteredSchedules = schedules.filterNot { it.scheduleId in idsToRemove }
                if (filteredSchedules.isNotEmpty()) {
                    updatedSchedules[date] = filteredSchedules
                }
            }
            currentSchedules.clear()
            currentSchedules.putAll(updatedSchedules)
        }

        val schedulesToAdd = added + updated
        if (schedulesToAdd.isNotEmpty()) {
            schedulesToAdd.groupBy { schedule ->
                Instant.fromEpochSeconds(schedule.start)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            }.forEach { (date, schedules) ->
                currentSchedules[date.time()] = mergeAndSortSchedules(
                    schedules,
                    currentSchedules[date.time()] ?: emptyList()
                )
            }
        }

        _schedulesByDate.update { currentSchedules.toMap() }
        invalidateCache()
    }

    private fun mergeAndSortSchedules(
        newSchedules: List<ScheduleModel>,
        existingSchedules: List<ScheduleModel>
    ): List<ScheduleModel> {
        val existingMap = existingSchedules.associateBy { it.scheduleId }
        val newMap = newSchedules.associateBy { it.scheduleId }

        return (existingMap + newMap).values.sortedBy { it.start }
    }

    private fun invalidateCache() {
        sortedSchedulesCache.clear()
        cacheVersion++
    }
}

