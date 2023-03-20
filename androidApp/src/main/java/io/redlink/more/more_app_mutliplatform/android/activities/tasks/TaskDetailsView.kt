package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel

@Composable
fun TaskDetailsView(viewModel: TaskDetailsViewModel, observationId: String?, scheduleId: String?) {
    viewModel.loadTaskDetails(observationId, scheduleId)
    val taskDetails: MutableState<TaskDetailsModel?> = viewModel.taskDetailsModel
    
    Text(text = taskDetails.value?.observationTitle?: "")
}