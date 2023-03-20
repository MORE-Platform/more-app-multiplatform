package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.extensions.jvmLocalDate
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.ScheduleState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ScheduleViewModel(observationFactory: ObservationFactory) : ViewModel() {
    private val coreViewModel = CoreScheduleViewModel(observationFactory)

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
            coreViewModel.activeScheduleState.collect {
                activeScheduleState.clear()
                activeScheduleState.putAll(it)
            }
        }
    }

    fun startObservation(scheduleModel: ScheduleModel) {
        coreViewModel.start(scheduleModel)
    }

    fun pauseObservation(scheduleId: String) {
        coreViewModel.pause(scheduleId)
    }

    fun stopObservation(scheduleId: String) {
        coreViewModel.stop(scheduleId)
    }

    private fun updateData(data: Map<LocalDate, List<ScheduleModel>>) {
        val filteredData =
            data.filter { entry -> entry.key >= LocalDate.now() && entry.value.isNotEmpty() }.toSortedMap()
        schedules.clear()
        schedules.putAll(filteredData)
    }

    private fun removeSchedule(scheduleId: String) {
        coreViewModel.removeSchedule(scheduleId = scheduleId)
    }
}