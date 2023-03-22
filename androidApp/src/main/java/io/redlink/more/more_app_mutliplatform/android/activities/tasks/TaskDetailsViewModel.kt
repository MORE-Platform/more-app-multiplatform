package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.android.services.ObservationRecordingService
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.ScheduleState
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*

class TaskDetailsViewModel(
    dataRecorder: DataRecorder
) {

    private val coreViewModel: CoreTaskDetailsViewModel = CoreTaskDetailsViewModel(dataRecorder)
    var isEnabled: MutableState<Boolean> = mutableStateOf(false)
    var dataPointCount: MutableState<Long> = mutableStateOf(0)
    var taskDetailsModel: MutableState<TaskDetailsModel> = mutableStateOf(
        TaskDetailsModel(
            "", "", "", "", 0, 0, "", MutableStateFlow(DataPointCountSchema())
        )
    )
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    fun loadTaskDetails(observationId: String?, scheduleId: String?) {
        if (observationId != null && scheduleId != null) {
            scope.launch {
                coreViewModel.loadTaskDetails(observationId, scheduleId)
                coreViewModel.taskDetailsModel.collect { details ->
                    details?.let {
                        taskDetailsModel = mutableStateOf(it)
                        dataPointCount = mutableStateOf(coreViewModel.loadDataPointCount().value)
                        isEnabled = mutableStateOf(taskDetailsModel.value.start.toDate() <= Date() && Date() < taskDetailsModel.value.end.toDate())
                    }
                }
            }
        }
    }
}