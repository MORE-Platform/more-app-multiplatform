package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.extensions.jvmLocalDate
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ScheduleViewModel(private val observationFactory: ObservationFactory) : ViewModel() {
    private val coreViewModel = CoreScheduleViewModel()

    val schedules = mutableStateMapOf<LocalDate, List<ScheduleModel>>()

    val activeScheduleState = mutableStateMapOf<String, ScheduleState>()

    private val observationMap = mutableMapOf<String, Observation>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.scheduleModelList.collect { map ->
                val javaConvertedMap = map.mapKeys { it.key.jvmLocalDate() }
                withContext(Dispatchers.Main) {
                    updateData(javaConvertedMap)
                }
            }
        }
    }

    fun startObservation(scheduleId: String, observationId: String, type: String) {
        if (observationMap[scheduleId] != null && observationMap[scheduleId]?.start(observationId) == true) {
            activeScheduleState[scheduleId] = ScheduleState.RUNNING
        } else {
            observationFactory.observation(observationId, type)?.let {
                observationMap[scheduleId] = it
                if (it.start(observationId)) {
                    activeScheduleState[scheduleId] = ScheduleState.RUNNING
                }
            }
        }
    }

    fun pauseObservation(scheduleId: String) {
        observationMap[scheduleId]?.let {
            stopSensor(it)
            activeScheduleState[scheduleId] = ScheduleState.PAUSED
        }
    }

    fun stopObservation(scheduleId: String) {
        observationMap[scheduleId]?.let {
            stopSensor(it)
            activeScheduleState[scheduleId] = ScheduleState.STOPPED
            observationMap.remove(scheduleId)
        }
    }

    private fun stopSensor(observation: Observation) {
        observation.stop()
        observation.finish()
    }

    private fun updateData(data: Map<LocalDate, List<ScheduleModel>>) {
        val filteredData =
            data.filter { entry -> entry.key >= LocalDate.now() && entry.value.isNotEmpty() }.toSortedMap()
        schedules.clear()
        schedules.putAll(filteredData)
    }

    private fun removeSchedule(scheduleId: String) {
        stopObservation(scheduleId)
        val data = schedules.mapValues { entry ->
            entry.value
                .filterNot { model -> model.scheduleId == scheduleId }
        }
        updateData(data)
    }
}