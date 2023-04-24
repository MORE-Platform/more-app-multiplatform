package io.redlink.more.app.android.activities.dashboard.schedule

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.extensions.jvmLocalDate
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.app.android.services.ObservationRecordingService
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ScheduleViewModel(coreFilterModel: CoreDashboardFilterViewModel, dataRecorder: AndroidDataRecorder,
                        private val scheduleListType: ScheduleListType) : ViewModel() {

    val coreViewModel = CoreScheduleViewModel(dataRecorder, scheduleListType)
    private val coreFilterViewModel = coreFilterModel

    val polarHrReady: MutableState<Boolean> = mutableStateOf(false)

    val schedules = mutableStateMapOf<LocalDate, List<ScheduleModel>>()

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
            PolarHeartRateObservation.hrReady.collect {
                withContext(Dispatchers.Main) {
                    polarHrReady.value = it
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
        return if (!hasNoFilters() && scheduleListType == ScheduleListType.ALL) return filteredSchedules else schedules
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