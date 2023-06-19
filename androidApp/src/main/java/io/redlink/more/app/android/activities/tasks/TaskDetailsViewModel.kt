package io.redlink.more.app.android.activities.tasks

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskDetailsViewModel(
    dataRecorder: DataRecorder
): ViewModel() {
    private val coreViewModel: CoreTaskDetailsViewModel = CoreTaskDetailsViewModel(dataRecorder)
    val isEnabled = mutableStateOf(false)
    val polarHrReady = mutableStateOf(false)
    val dataPointCount = mutableStateOf(0L)
    val taskDetailsModel = mutableStateOf(
        TaskDetailsModel(
            "", "", "", "", 0, 0, "", false, ScheduleState.DEACTIVATED
        )
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            PolarHeartRateObservation.hrReady.collect {
                withContext(Dispatchers.Main) {
                    polarHrReady.value = it
                }
            }
        }
        viewModelScope.launch {
            coreViewModel.taskDetailsModel.collect { details ->
                details?.let {
                    withContext(Dispatchers.Main) {
                        taskDetailsModel.value = it
                        isEnabled.value = it.state.active()
                    }
                }
            }
        }
        viewModelScope.launch {
            coreViewModel.dataCount.collect {
                withContext(Dispatchers.Main) {
                    dataPointCount.value = it
                }
            }
        }
    }

    fun setSchedule(scheduleId: String) {
        coreViewModel.setSchedule(scheduleId)
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }

    fun startObservation() {
        coreViewModel.startObservation()
    }

    fun pauseObservation() {
        coreViewModel.pauseObservation()
    }

    fun stopObservation() {
        coreViewModel.stopObservation()
    }
}