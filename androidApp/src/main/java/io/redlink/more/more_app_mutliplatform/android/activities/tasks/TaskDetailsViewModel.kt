package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
            "", "", "", "", 0, 0, "", ScheduleState.DEACTIVATED,0
        )
    )
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        scope.launch {
            coreViewModel.taskDetailsModel.collect { details ->
                details?.let {
                    taskDetailsModel.value = it
                    dataPointCount.value = taskDetailsModel.value.dataPointCount
                    isEnabled.value = taskDetailsModel.value.start.toDate() <= Date() && Date() < taskDetailsModel.value.end.toDate()
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