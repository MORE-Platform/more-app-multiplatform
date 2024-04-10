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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Square
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.jvmLocalDate
import io.redlink.more.app.android.extensions.jvmLocalDateTime
import io.redlink.more.app.android.shared_composables.Accordion
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.DatapointCollectionView
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.shared_composables.SmallTextIconButton
import io.redlink.more.app.android.shared_composables.TimeframeDays
import io.redlink.more.app.android.shared_composables.TimeframeHours
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.moreSecondary2
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.LimeSurveyType
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.PolarVerityHeartRateType
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.SimpleQuestionType


@Composable
fun TaskDetailsView(
    navController: NavController,
    viewModel: TaskDetailsViewModel,
    scheduleId: String?
) {
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.SCHEDULE_DETAILS.routeWithParameters())
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }
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
                viewModel.taskDetailsModel.value.start.jvmLocalDate(),
                viewModel.taskDetailsModel.value.end.jvmLocalDate(),
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
            TimeframeHours(
                viewModel.taskDetailsModel.value.start.jvmLocalDateTime(),
                viewModel.taskDetailsModel.value.end.jvmLocalDateTime(),
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
                    if (!viewModel.taskDetailsModel.value.state.completed()) {
                        DatapointCollectionView(
                            viewModel.dataPointCount.value,
                            viewModel.taskDetailsModel.value.state
                        )
                        if (!viewModel.taskDetailsModel.value.hidden) {
                            Spacer(modifier = Modifier.height(20.dp))
                            SmallTextButton(
                                text = if (viewModel.taskDetailsModel.value.state == ScheduleState.RUNNING) getStringResource(
                                    id = R.string.more_observation_pause
                                )
                                else if (viewModel.taskDetailsModel.value.observationType == SimpleQuestionType().observationType) getStringResource(
                                    id = R.string.more_questionnaire_start
                                )
                                else if (viewModel.taskDetailsModel.value.observationType == LimeSurveyType().observationType) getStringResource(
                                    id = R.string.more_limesurvey_start
                                )
                                else getStringResource(
                                    id = R.string.more_observation_start
                                ),
                                enabled = viewModel.isEnabled.value && if (viewModel.taskDetailsModel.value.observationType == PolarVerityHeartRateType(
                                        emptySet()
                                    ).observationType) viewModel.polarHrReady.value else true
                            ) {
                                if (viewModel.taskDetailsModel.value.observationType == SimpleQuestionType().observationType) {
                                    navController.navigate(NavigationScreen.SIMPLE_QUESTION.navigationRoute("scheduleId" to scheduleId))
                                }
                                else if (viewModel.taskDetailsModel.value.observationType == LimeSurveyType().observationType) {
                                    navController.navigate(NavigationScreen.LIMESURVEY.navigationRoute("scheduleId" to scheduleId))
                                } else if (viewModel.taskDetailsModel.value.state == ScheduleState.RUNNING) {
                                    viewModel.pauseObservation()
                                } else {
                                    viewModel.startObservation()
                                }
                            }
                        }
                    }
            }
        }
    }
}