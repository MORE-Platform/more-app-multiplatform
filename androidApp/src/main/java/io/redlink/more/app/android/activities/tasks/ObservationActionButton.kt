package io.redlink.more.app.android.activities.tasks

import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.theme.MoreColors
import io.redlink.more.app.android.theme.moreApproved
import io.redlink.more.app.android.theme.morePrimary
import io.redlink.more.models.ScheduleState
import io.redlink.more.navigation.model.NavigationRouteParameter
import io.redlink.more.observations.observationTypes.LimeSurveyType
import io.redlink.more.observations.observationTypes.QuestionType

@Composable
fun ObservationActionButton(
    navController: NavController,
    scheduleId: String,
    observationType: String,
    scheduleState: ScheduleState,
    additionalEnableCondition: Boolean = true,
    onClick: () -> Unit
) {

    if (QuestionType().matches(observationType)) {
        SmallTextButton(
            text = getStringResource(id = R.string.more_questionnaire_start),
            enabled = scheduleState.active(),
            borderStroke = MoreColors.borderApproved(),
            buttonColors = ButtonDefaults.moreApproved()
        ) {
            navController.navigate(
                NavigationScreen.QUESTION.navigationRoute(NavigationRouteParameter.SCHEDULE_ID.key to scheduleId)
            )
        }
    } else if (LimeSurveyType().matches(observationType)) {
        SmallTextButton(
            text = getStringResource(id = R.string.more_limesurvey_start),
            enabled = scheduleState.active(),
            borderStroke = MoreColors.borderApproved(),
            buttonColors = ButtonDefaults.moreApproved()
        ) {
            navController.navigate(
                NavigationScreen.LIMESURVEY.navigationRoute(
                    NavigationRouteParameter.SCHEDULE_ID.key to scheduleId
                )
            )
        }
    } else {
        val isRunning = scheduleState == ScheduleState.RUNNING
        SmallTextButton(
            text = if (isRunning) getStringResource(
                id = R.string.more_observation_pause
            ) else getStringResource(
                id = R.string.more_observation_start
            ),
            enabled = scheduleState.active() && additionalEnableCondition,
            borderStroke = if (isRunning) MoreColors.borderPrimary(scheduleState.active() && additionalEnableCondition) else MoreColors.borderApproved(),
            buttonColors = if (isRunning) ButtonDefaults.morePrimary() else ButtonDefaults.moreApproved()
        ) {
            onClick()
        }
    }
}