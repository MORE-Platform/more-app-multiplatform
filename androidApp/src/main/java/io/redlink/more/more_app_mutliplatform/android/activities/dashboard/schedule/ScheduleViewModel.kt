package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.extensions.jvmLocalDate
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidDataRecorder
import io.redlink.more.more_app_mutliplatform.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.more_app_mutliplatform.android.services.ObservationRecordingService
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.ZoneId

class ScheduleViewModel(androidDataRecorder: AndroidDataRecorder, coreFilterModel: CoreDashboardFilterViewModel) : ViewModel() {
    private val coreViewModel = CoreScheduleViewModel(androidDataRecorder)
    private val coreFilterViewModel = coreFilterModel

    val polarHrReady: MutableState<Boolean> = mutableStateOf(false)

    val schedules = mutableStateMapOf<LocalDate, List<ScheduleModel>>()
    val filteredSchedules = mutableStateMapOf<LocalDate, List<ScheduleModel>>()

    val activeScheduleState = mutableStateMapOf<String, ScheduleState>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.scheduleModelList.collect { map ->
                val javaConvertedMap = map.mapKeys { it.key.jvmLocalDate() }
                withContext(Dispatchers.Main) {
                    updateData(javaConvertedMap)
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                PolarHeartRateObservation.hrReady.collect {
                    polarHrReady.value = it
                }
            }
        }
        viewModelScope.launch (Dispatchers.IO) {
            coreFilterViewModel.currentFilter.collect{
                withContext(Dispatchers.Main) {
                    updateFilteredData(schedules)
                }
            }
        }
    }

    fun updateTaskStates(context: Context) {
        ObservationRecordingService.updateTaskStates(context)
    }

    fun startObservation(context: Context, scheduleId: String) {
        ObservationRecordingService.start(context, scheduleId)
        activeScheduleState[scheduleId] = ScheduleState.RUNNING
    }

    fun pauseObservation(context: Context, scheduleId: String) {
        ObservationRecordingService.pause(context, scheduleId)
        activeScheduleState[scheduleId] = ScheduleState.PAUSED
    }

    fun stopObservation(context: Context, scheduleId: String) {
        ObservationRecordingService.stop(context, scheduleId)
    }

    private fun updateData(data: Map<LocalDate, List<ScheduleModel>>) {
        schedules.clear()
        schedules.putAll(data.toSortedMap())
    }

    private fun updateFilteredData(data: Map<LocalDate, List<ScheduleModel>>) {
        filteredSchedules.clear()
        filteredSchedules.putAll(applyFilter(schedules))
    }

    private fun removeSchedule(scheduleId: String) {
        coreViewModel.removeSchedule(scheduleId = scheduleId)
    }

    fun getScheduleMap(): SnapshotStateMap<LocalDate, List<ScheduleModel>> {
        return if(hasNoFilters())
             schedules
        else filteredSchedules
    }

    fun hasNoFilters() = coreFilterViewModel.hasDateFilter(DateFilterModel.ENTIRE_TIME) && coreFilterViewModel.hasAllTypes()

    private fun applyFilter(scheduleModelList: Map<LocalDate, List<ScheduleModel>>): Map<LocalDate, List<ScheduleModel>> {
        val scheduleModelListAsLong = scheduleModelList.mapKeys {
            it.key.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        return coreFilterViewModel.applyFilter(scheduleModelListAsLong).mapKeys {
            it.key.jvmLocalDate()
        }
    }
}