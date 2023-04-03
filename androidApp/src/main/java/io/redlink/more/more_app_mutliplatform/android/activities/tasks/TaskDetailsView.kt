package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.SmallTextButton
import io.redlink.more.more_app_mutliplatform.models.ScheduleState

@Composable
fun TaskDetailsView(viewModel: TaskDetailsViewModel, scheduleId: String?) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Text(text = viewModel.taskDetailsModel.value.observationTitle)
        Text(text = viewModel.taskDetailsModel.value.observationType)
        Text(text = "start: ${viewModel.taskDetailsModel.value.start}")
        Text(text = "start: ${viewModel.taskDetailsModel.value.end}")
        Text(text = viewModel.taskDetailsModel.value.participantInformation)

        scheduleId?.let {
            SmallTextButton(
                text = if (viewModel.taskDetailsModel.value.state == ScheduleState.RUNNING) getStringResource(id = R.string.more_observation_pause) else getStringResource(
                    id = R.string.more_observation_start
                ), enabled = viewModel.isEnabled.value
            ) {
                if (viewModel.taskDetailsModel.value.state == ScheduleState.RUNNING) {
                    viewModel.pauseObservation()
                } else {
                    viewModel.startObservation()
                }
            }
        }
    }
}