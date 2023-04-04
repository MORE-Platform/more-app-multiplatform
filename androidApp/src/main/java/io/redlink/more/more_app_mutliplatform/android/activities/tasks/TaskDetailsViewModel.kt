package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.*

class TaskDetailsViewModel(
    scheduleId: String,
    dataRecorder: DataRecorder
) {

    private val coreViewModel: CoreTaskDetailsViewModel = CoreTaskDetailsViewModel(scheduleId, dataRecorder)
    val isEnabled= mutableStateOf(false)
    val dataPointCount = mutableStateOf(0L)
    val taskDetailsModel = mutableStateOf(
        TaskDetailsModel(
            "", "", "", "", 0, 0, "", ScheduleState.DEACTIVATED,
        )
    )
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        scope.launch {
            coreViewModel.taskDetailsModel.collect { details ->
                details?.let {
                    withContext(Dispatchers.Main) {
                        taskDetailsModel.value = it
                        isEnabled.value = taskDetailsModel.value.start.toDate() <= Date() && Date() < taskDetailsModel.value.end.toDate()
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