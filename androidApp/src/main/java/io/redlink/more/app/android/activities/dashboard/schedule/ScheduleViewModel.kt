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
package io.redlink.more.app.android.activities.dashboard.schedule

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.dashboard.filter.DashboardFilterViewModel
import io.redlink.more.app.android.extensions.jvmLocalDate
import io.redlink.more.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ScheduleViewModel(
    val scheduleListType: ScheduleListType
) : ViewModel() {
    private val coreDashboardFilterViewModel = CoreDashboardFilterViewModel()

    private val coreViewModel = CoreScheduleViewModel(
        MoreApplication.shared!!.dataRecorder,
        coreFilterModel = coreDashboardFilterViewModel,
        scheduleListType = scheduleListType
    )

    val polarHrReady: MutableState<Boolean> = mutableStateOf(false)

    val schedulesByDate = mutableStateMapOf<LocalDate, List<ScheduleModel>>()
    val observationErrors = mutableStateMapOf<String, Set<String>>()

    val filterModel = DashboardFilterViewModel(coreDashboardFilterViewModel)

    private val jobs = mutableListOf<Job>()

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
        jobs.add(viewModelScope.launch {
            coreViewModel.scheduleListState.collect { (added, removed, updated) ->
                val idsToRemove = removed + updated.map { it.scheduleId }.toSet()
                schedulesByDate.forEach { (date, schedules) ->
                    schedulesByDate[date] = schedules.filterNot { it.scheduleId in idsToRemove }
                }
                val schemasToAdd = mergeSchedules(added, updated)
                schemasToAdd.groupBy { it.start.jvmLocalDate() }.forEach { (date, schedules) ->
                    schedulesByDate[date] = mergeSchedules(
                        schedules.toSet(),
                        schedulesByDate.getOrDefault(date, emptyList()).toSet()
                    ).sortedBy { it.start }
                }
            }
        })
        jobs.add(viewModelScope.launch(Dispatchers.IO) {
            PolarHeartRateObservation.hrReady.collect {
                withContext(Dispatchers.Main) {
                    polarHrReady.value = it
                }
            }
        })
        jobs.add(viewModelScope.launch(Dispatchers.IO) {
            MoreApplication.shared!!.observationFactory.observationErrors.collect {
                withContext(Dispatchers.Main) {
                    observationErrors.clear()
                    observationErrors.putAll(it)
                }
            }
        })
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    fun startObservation(scheduleId: String) {
        coreViewModel.start(scheduleId)
    }

    fun pauseObservation(scheduleId: String) {
        coreViewModel.pause(scheduleId)
    }

    fun stopObservation(scheduleId: String) {
        coreViewModel.stop(scheduleId)
    }


    private fun mergeSchedules(
        first: Set<ScheduleModel>,
        second: Set<ScheduleModel>
    ): Set<ScheduleModel> {
        val firstIds = first.map { it.scheduleId }.toSet()
        val secondFiltered = second.filter { it.scheduleId !in firstIds }
        return first + secondFiltered
    }
}