package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.SmallTextButton
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.ScheduleState

@Composable
fun TaskDetailsView(viewModel: TaskDetailsViewModel, scheduleViewModel: ScheduleViewModel, observationId: String?, scheduleId: String?) {
    viewModel.loadTaskDetails(observationId, scheduleId)
    val taskDetails: MutableState<TaskDetailsModel> = viewModel.taskDetailsModel
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Text(text = taskDetails.value.observationTitle)
        Text(text = taskDetails.value.observationType)
        Text(text = "start: ${taskDetails.value.start}")
        Text(text = "start: ${taskDetails.value.end}")
        Text(text = taskDetails.value.participantInformation)
        SmallTextButton(
            text = if (scheduleViewModel.activeScheduleState[scheduleId?: ""] == ScheduleState.RUNNING) getStringResource(id = R.string.more_observation_pause) else getStringResource(
                id = R.string.more_observation_start
            ), enabled = viewModel.isEnabled.value
        ) {
            if (scheduleViewModel.activeScheduleState[scheduleId?: ""] == ScheduleState.RUNNING) {
                scheduleViewModel.pauseObservation(context, taskDetails.value.scheduleId)
            } else {
                scheduleViewModel.startObservation(
                    context,
                    taskDetails.value.scheduleId
                )
            }
        }
    }
}