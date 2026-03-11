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
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.SmallTextButton
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
            enabled = scheduleState.active()
        ) {
            navController.navigate(
                NavigationScreen.QUESTION.navigationRoute(NavigationRouteParameter.SCHEDULE_ID.key to scheduleId)
            )
        }
    } else if (LimeSurveyType().matches(observationType)) {
        SmallTextButton(
            text = getStringResource(id = R.string.more_limesurvey_start),
            enabled = scheduleState.active()
        ) {
            navController.navigate(
                NavigationScreen.LIMESURVEY.navigationRoute(
                    NavigationRouteParameter.SCHEDULE_ID.key to scheduleId
                )
            )
        }
    } else {
        SmallTextButton(
            text = if (scheduleState == ScheduleState.RUNNING) getStringResource(
                id = R.string.more_observation_pause
            ) else getStringResource(
                id = R.string.more_observation_start
            ),
            enabled = scheduleState.active() && additionalEnableCondition
        ) {
            onClick()
        }
    }
}