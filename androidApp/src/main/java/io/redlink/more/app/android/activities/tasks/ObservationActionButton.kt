/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */

package io.redlink.more.app.android.activities.tasks

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.logging.event
import io.redlink.more.models.ScheduleState
import io.redlink.more.navigation.model.NavigationRouteParameter
import io.redlink.more.observations.appUsage.model.LogEvent
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
    SmallTextButton(
        text = buttonText(observationType = observationType, scheduleState = scheduleState),
        enabled = scheduleState.active() && buttonEnabled(
            observationType = observationType,
            additionalEnableCondition = additionalEnableCondition
        )
    ) {
        handleButtonAction(
            navController = navController,
            scheduleId = scheduleId,
            observationType = observationType,
            scheduleState = scheduleState,
            onClick = onClick
        )
    }
}

@Composable
private fun buttonText(
    observationType: String,
    scheduleState: ScheduleState
): String {
    return when {
        QuestionType().matches(observationType) -> getStringResource(id = R.string.more_questionnaire_start)
        LimeSurveyType().matches(observationType) -> getStringResource(id = R.string.more_limesurvey_start)
        scheduleState == ScheduleState.RUNNING -> getStringResource(id = R.string.more_observation_pause)
        else -> getStringResource(id = R.string.more_observation_start)
    }
}

private fun buttonEnabled(
    observationType: String,
    additionalEnableCondition: Boolean
): Boolean {
    val opensSeparateScreen =
        QuestionType().matches(observationType) || LimeSurveyType().matches(observationType)

    return if (opensSeparateScreen) {
        true
    } else {
        additionalEnableCondition
    }
}

private fun handleButtonAction(
    navController: NavController,
    scheduleId: String,
    observationType: String,
    scheduleState: ScheduleState,
    onClick: () -> Unit
) {
    val navigationScreen = when {
        QuestionType().matches(observationType) -> NavigationScreen.QUESTION
        LimeSurveyType().matches(observationType) -> NavigationScreen.LIMESURVEY
        else -> null
    }

    Napier.event(
        LogEvent.BUTTON_PRESS,
        "${if (scheduleState == ScheduleState.RUNNING) "Pause" else "Start"} observation $observationType"
    )

    if (navigationScreen != null) {
        navController.navigate(
            navigationScreen.navigationRoute(
                NavigationRouteParameter.SCHEDULE_ID.key to scheduleId
            )
        )
    } else {
        onClick()
    }
}