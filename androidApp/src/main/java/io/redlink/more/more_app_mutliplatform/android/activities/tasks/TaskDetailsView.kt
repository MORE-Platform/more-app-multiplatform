package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Square
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.NavigationScreen
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.android.shared_composables.*
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.ui.theme.moreSecondary2
import io.redlink.more.more_app_mutliplatform.models.ScheduleState

@Composable
fun TaskDetailsView(navController: NavController, viewModel: TaskDetailsViewModel, scheduleId: String?, observationTitle: String) {
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                HeaderTitle(
                    title = viewModel.taskDetailsModel.value.observationTitle,
                    modifier = Modifier
                        .weight(0.65f)
                        .padding(vertical = 11.dp)
                )
                if (viewModel.taskDetailsModel.value.state == ScheduleState.RUNNING)
                    SmallTextIconButton(
                        text = getStringResource(id = R.string.more_abort),
                        imageText = getStringResource(id = R.string.more_abort),
                        image = Icons.Rounded.Square,
                        imageTint = MoreColors.Important,
                        borderStroke = MoreColors.borderDefault(),
                        buttonColors = ButtonDefaults.moreSecondary2()
                    ) {
                        viewModel.stopObservation()
                    }
            }
            BasicText(
                text = viewModel.taskDetailsModel.value.observationType,
                color = MoreColors.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            )

            TimeframeDays(
                viewModel.taskDetailsModel.value.start.toDate(),
                viewModel.taskDetailsModel.value.end.toDate(),
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
            TimeframeHours(
                viewModel.taskDetailsModel.value.start.toDate(),
                viewModel.taskDetailsModel.value.end.toDate(),
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Accordion(
                title = getStringResource(id = R.string.participant_information),
                description = viewModel.taskDetailsModel.value.participantInformation,
                hasCheck = false,
                hasPreview = false
            )
            Spacer(modifier = Modifier.height(8.dp))

            scheduleId?.let {
                DatapointCollectionView(viewModel.dataPointCount.value, viewModel.taskDetailsModel.value.state)
                Spacer(modifier = Modifier.height(20.dp))

                SmallTextButton(
                    text = if (viewModel.taskDetailsModel.value.state == ScheduleState.RUNNING) getStringResource(
                        id = R.string.more_observation_pause
                    ) else getStringResource(
                        id = R.string.more_observation_start
                    ), enabled = viewModel.isEnabled.value && if (viewModel.taskDetailsModel.value.observationType == "polar-verity-observation") viewModel.polarHrReady.value else true
                ) {
                    if (viewModel.taskDetailsModel.value.observationType == "question-observation")
                        navController.navigate("${NavigationScreen.SIMPLE_QUESTION.route}/scheduleId=${scheduleId}",)
                    else if (viewModel.taskDetailsModel.value.state == ScheduleState.RUNNING) {
                        viewModel.pauseObservation()
                    } else {
                        viewModel.startObservation()
                    }
                }
            }
        }
    }
}