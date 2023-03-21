package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.android.shared_composables.SmallTextButton
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.ScheduleState
import java.util.*

@Composable
fun TaskDetailsView(viewModel: TaskDetailsViewModel, observationId: String?, scheduleId: String?, scheduleState: ScheduleState) {
    viewModel.loadTaskDetails(observationId, scheduleId, scheduleState)
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
            text = if (viewModel.scheduleState.value == ScheduleState.RUNNING) getStringResource(id = R.string.more_observation_pause) else getStringResource(
                id = R.string.more_observation_start
            ), enabled = viewModel.isEnabled.value
        ) {
            if (viewModel.scheduleState.value == ScheduleState.RUNNING) {
                viewModel.pauseObservation(context, taskDetails.value.scheduleId)
            } else {
                viewModel.startObservation(
                    context,
                    taskDetails.value.scheduleId
                )
            }
        }
    }
}