package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import android.widget.ProgressBar
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.android.shared_composables.*
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.ScheduleState

@Composable
fun TaskDetailsView(viewModel: TaskDetailsViewModel, scheduleViewModel: ScheduleViewModel, observationId: String?, scheduleId: String?) {
    viewModel.loadTaskDetails(observationId, scheduleId)
    val taskDetails: MutableState<TaskDetailsModel> = viewModel.taskDetailsModel
    val datapointsCollected: MutableState<Long> = viewModel.dataPointCount
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(4.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)){
            HeaderTitle(title = taskDetails.value.observationTitle)
            //TODO AbortButton()
        }
        BasicText(
            text = taskDetails.value.observationType,
            color = MoreColors.Secondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp))

        TimeframeDays(taskDetails.value.start.toDate(),
            taskDetails.value.end.toDate(),
            Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp))
        TimeframeHours(taskDetails.value.start.toDate(),
            taskDetails.value.end.toDate(),
            Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Accordion(
            title = getStringResource(id = R.string.participant_information),
            description = taskDetails.value.participantInformation,
            hasCheck = false,
            hasPreview = false
        )
        Spacer(modifier = Modifier.height(8.dp))

        scheduleId?.let {
            if (scheduleViewModel.activeScheduleState[scheduleId] == ScheduleState.RUNNING) {
                DatapointCollectionView(datapointsCollected)
            }

            SmallTextButton(
                text = if (scheduleViewModel.activeScheduleState[scheduleId] == ScheduleState.RUNNING) getStringResource(id = R.string.more_observation_pause) else getStringResource(
                    id = R.string.more_observation_start
                ), enabled = viewModel.isEnabled.value
            ) {
                if (scheduleViewModel.activeScheduleState[scheduleId] == ScheduleState.RUNNING) {
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
}