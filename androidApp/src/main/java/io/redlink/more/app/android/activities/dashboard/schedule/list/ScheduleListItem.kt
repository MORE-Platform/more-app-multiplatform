package io.redlink.more.app.android.activities.dashboard.schedule.list

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.shared_composables.SmallTitle
import io.redlink.more.app.android.shared_composables.TimeframeHours
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.observations.limeSurvey.LimeSurveyActivity
import io.redlink.more.app.android.extensions.jvmLocalDateTime


@Composable
fun ScheduleListItem(
    navController: NavController,
    scheduleModel: ScheduleModel,
    viewModel: ScheduleViewModel,
    showButton: Boolean
) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            SmallTitle(text = scheduleModel.observationTitle, color = MoreColors.Primary)
            if (scheduleModel.scheduleState == ScheduleState.RUNNING) {
                CircularProgressIndicator(
                    color = MoreColors.Approved,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .height(15.dp)
                        .width(15.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicText(text = scheduleModel.observationType, color = MoreColors.Secondary)
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = getStringResource(id = R.string.more_schedule_details),
                tint = MoreColors.Primary
            )
        }

        TimeframeHours(
            startTime = scheduleModel.start.jvmLocalDateTime(),
            endTime = scheduleModel.end.jvmLocalDateTime(),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        if (showButton && !scheduleModel.hidden) {
            when (scheduleModel.observationType) {
                "self-learning-multiple-choice-question-observation" -> {
                    SmallTextButton(
                        text = getStringResource(id = R.string.more_questionnaire_start),
                        enabled = scheduleModel.scheduleState.active()
                    ) {
                        navController.navigate(
                            "${NavigationScreen.SELF_LEARNING_MULTIPLE_CHOICE_QUESTION.route}/scheduleId=${scheduleModel.scheduleId}"
                        )
                    }
                }
                "question-observation" -> {
                    SmallTextButton(
                        text = getStringResource(id = R.string.more_questionnaire_start),
                        enabled = scheduleModel.scheduleState.active()
                    ) {
                        navController.navigate(
                            "${NavigationScreen.SIMPLE_QUESTION.route}/scheduleId=${scheduleModel.scheduleId}"
                        )
                    }
                }
                "lime-survey-observation" -> {
                    SmallTextButton(
                        text = getStringResource(id = R.string.more_limesurvey_start),
                        enabled = scheduleModel.scheduleState.active()
                    ) {
                        (context as? Activity)?.let { activity ->
                            val intent = Intent(context, LimeSurveyActivity::class.java)
                            intent.putExtra(
                                LimeSurveyActivity.LIME_SURVEY_ACTIVITY_SCHEDULE_ID,
                                scheduleModel.scheduleId
                            )
                            activity.startActivity(intent)
                        }
                    }
                }
                else -> {
                    SmallTextButton(
                        text = if (scheduleModel.scheduleState == ScheduleState.RUNNING) getStringResource(
                            id = R.string.more_observation_pause
                        ) else getStringResource(
                            id = R.string.more_observation_start
                        ),
                        enabled = scheduleModel.scheduleState.active() && (if (scheduleModel.observationType == "polar-verity-observation") viewModel.polarHrReady.value else true)
                    ) {
                        if (scheduleModel.scheduleState == ScheduleState.RUNNING) {
                            viewModel.pauseObservation(scheduleModel.scheduleId)
                        } else {
                            viewModel.startObservation(scheduleModel.scheduleId)
                        }

                    }
                }
            }
        }
    }
}