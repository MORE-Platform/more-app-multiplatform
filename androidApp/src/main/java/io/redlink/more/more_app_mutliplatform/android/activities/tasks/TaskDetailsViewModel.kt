package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TaskDetailsViewModel(
    scheduleViewModel: CoreScheduleViewModel
) {

    private val coreViewModel: CoreTaskDetailsViewModel = CoreTaskDetailsViewModel(scheduleViewModel)
    var taskDetailsModel: MutableState<TaskDetailsModel?> = mutableStateOf(null)
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    fun loadTaskDetails(observationId: String?, scheduleId: String?) {
        if (observationId != null && scheduleId != null) {
            coreViewModel.loadTaskDetails(observationId, scheduleId)
            scope.launch {
                coreViewModel.taskDetailsModel.collect {
                    taskDetailsModel = mutableStateOf(it)
                }
            }
        }
    }
}