package io.redlink.more.app.android.activities.tasks

import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import io.redlink.more.observations.observationTypes.Polar360TempType
import io.redlink.more.observations.observationTypes.Polar360AccType
import io.redlink.more.observations.observationTypes.Polar360HrType
import io.redlink.more.observations.observationTypes.Polar360PpiType
import io.redlink.more.services.bluetooth.BluetoothStateManagement

@Composable
fun ObservationActionButton(
    navController: NavController,
    scheduleId: String,
    observationType: String,
    scheduleState: ScheduleState,
    taskObservationErrors: List<String> = emptyList(),
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
    }
    else if(Polar360TempType(emptySet()).matches(observationType) || Polar360AccType(emptySet()).matches(observationType)
        || Polar360HrType(emptySet()).matches(observationType) || Polar360PpiType(emptySet()).matches(observationType)
    ){
        val connectedDevices by BluetoothStateManagement.connectedDevices.collectAsStateWithLifecycle()
        val polar360DeviceConnected = connectedDevices.any { device ->
            val name = device.deviceName?.lowercase() ?: ""
            name.contains("polar") && name.contains("360")
        }
        val isRunning = scheduleState == ScheduleState.RUNNING
        val enabled = scheduleState.active() && polar360DeviceConnected && (!isRunning || taskObservationErrors.isEmpty())
        SmallTextButton(
            text = if (isRunning) getStringResource(
                id = R.string.more_observation_pause
            ) else getStringResource(
                id = R.string.more_observation_start
            ),
            enabled = enabled,
            borderStroke = if (isRunning) MoreColors.borderPrimary(enabled) else MoreColors.borderApproved(),
            buttonColors = if (isRunning) ButtonDefaults.morePrimary() else ButtonDefaults.moreApproved()
        ) {
            onClick()
        }
    }
    else {
        val isRunning = scheduleState == ScheduleState.RUNNING
        val enabled = scheduleState.active() && additionalEnableCondition
        SmallTextButton(
            text = if (isRunning) getStringResource(
                id = R.string.more_observation_pause
            ) else getStringResource(
                id = R.string.more_observation_start
            ),
            enabled = enabled,
            borderStroke = if (isRunning) MoreColors.borderPrimary(enabled) else MoreColors.borderApproved(),
            buttonColors = if (isRunning) ButtonDefaults.morePrimary() else ButtonDefaults.moreApproved()
        ) {
            onClick()
        }
    }
}