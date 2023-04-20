package io.redlink.more.app.android.activities.tasks

import androidx.compose.runtime.mutableStateOf
import io.redlink.more.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.*

class TaskDetailsViewModel(
    scheduleId: String,
    dataRecorder: DataRecorder
) {

    private val coreViewModel: CoreTaskDetailsViewModel = CoreTaskDetailsViewModel(scheduleId, dataRecorder)
    val isEnabled= mutableStateOf(false)
    val polarHrReady = mutableStateOf(false)
    val dataPointCount = mutableStateOf(0L)
    val taskDetailsModel = mutableStateOf(
        TaskDetailsModel(
            "", "", "", "", 0, 0, "", ScheduleState.DEACTIVATED,
        )
    )
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {

        scope.launch(Dispatchers.IO) {
            PolarHeartRateObservation.hrReady.collect {
                withContext(Dispatchers.Main) {
                    polarHrReady.value = it
                }
            }
        }

        scope.launch {
            coreViewModel.taskDetailsModel.collect { details ->
                details?.let {
                    withContext(Dispatchers.Main) {
                        taskDetailsModel.value = it
                        isEnabled.value = it.state.active()
                    }
                }
            }
        }
        scope.launch {
            coreViewModel.dataCount.collect {
                withContext(Dispatchers.Main) {
                    dataPointCount.value = it
                }
            }
        }
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