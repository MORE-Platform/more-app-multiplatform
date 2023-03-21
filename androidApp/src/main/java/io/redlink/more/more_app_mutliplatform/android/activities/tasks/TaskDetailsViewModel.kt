package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TaskDetailsViewModel(
    dataRecorder: DataRecorder
) {

    private val coreViewModel: CoreTaskDetailsViewModel = CoreTaskDetailsViewModel(dataRecorder)
    var taskDetailsModel: MutableState<TaskDetailsModel?> = mutableStateOf(null)
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    fun loadTaskDetails(observationId: String?, scheduleId: String?) {
        if (observationId != null && scheduleId != null) {
            scope.launch {
                coreViewModel.loadTaskDetails(observationId, scheduleId)
                    coreViewModel.taskDetailsModel.collect {
                    taskDetailsModel = mutableStateOf(it)
                }
            }
        }
    }
}