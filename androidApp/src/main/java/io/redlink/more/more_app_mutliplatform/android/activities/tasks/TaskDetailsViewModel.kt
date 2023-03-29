package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class TaskDetailsViewModel(
    dataRecorder: DataRecorder
) {

    private val coreViewModel: CoreTaskDetailsViewModel = CoreTaskDetailsViewModel(dataRecorder)
    val isEnabled: MutableState<Boolean> = mutableStateOf(false)
    val dataPointCount: MutableState<Long> = mutableStateOf(0)
    val taskDetailsModel: MutableState<TaskDetailsModel> = mutableStateOf(
        TaskDetailsModel(
            "", "", "", "", 0, 0, "", DataPointCountSchema()
        )
    )
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    fun loadTaskDetails(observationId: String?, scheduleId: String?) {
        if (observationId != null && scheduleId != null) {
            scope.launch {
                coreViewModel.loadTaskDetails(observationId, scheduleId)
                coreViewModel.taskDetailsModel.collect { details ->
                    details?.let {
                        taskDetailsModel.value = it
                        taskDetailsModel.value.dataPointCount?.let { count ->
                            dataPointCount.value = count.count
                        } ?: run {dataPointCount.value = 0}
                        isEnabled.value = taskDetailsModel.value.start.toDate() <= Date() && Date() < taskDetailsModel.value.end.toDate()
                    }
                }
            }
        }
    }
}