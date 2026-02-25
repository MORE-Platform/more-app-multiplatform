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
package io.redlink.umm.blendedcare.app.android.activities.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.redlink.umm.blendedcare.app.android.MoreApplication
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.activities.NavigationScreen
import io.redlink.umm.blendedcare.app.android.activities.observationErrors.ObservationErrorListView
import io.redlink.umm.blendedcare.app.android.extensions.getStringResource
import io.redlink.umm.blendedcare.app.android.extensions.jvmLocalDate
import io.redlink.umm.blendedcare.app.android.extensions.jvmLocalDateTime
import io.redlink.umm.blendedcare.app.android.shared_composables.Accordion
import io.redlink.umm.blendedcare.app.android.shared_composables.BasicText
import io.redlink.umm.blendedcare.app.android.shared_composables.DatapointCollectionView
import io.redlink.umm.blendedcare.app.android.shared_composables.HeaderTitle
import io.redlink.umm.blendedcare.app.android.shared_composables.SmallTextButton
import io.redlink.umm.blendedcare.app.android.shared_composables.SmallTextIconButton
import io.redlink.umm.blendedcare.app.android.shared_composables.TimeframeDays
import io.redlink.umm.blendedcare.app.android.shared_composables.TimeframeHours
import io.redlink.umm.blendedcare.app.android.theme.MoreColors
import io.redlink.umm.blendedcare.app.android.theme.moreSecondary2
import io.redlink.umm.participant.models.ScheduleState
import io.redlink.umm.participant.observations.observationTypes.LimeSurveyType
import io.redlink.umm.participant.observations.observationTypes.PolarVerityHeartRateType
import io.redlink.umm.participant.observations.observationTypes.QuestionType

@Composable
fun TaskDetailsView(
    navController: NavController,
    scheduleId: String
) {
    val viewModel =
        remember {
            TaskDetailsViewModel(
                MoreApplication.shared!!.dataRecorder,
                MoreApplication.shared!!.observationFactory,
                scheduleId
            )
        }
    val taskDetails by viewModel.coreViewModel.taskDetailsModel.collectAsStateWithLifecycle()
    val dataPoints by viewModel.coreViewModel.dataCount.collectAsStateWithLifecycle()
    val taskErrors by viewModel.coreViewModel.taskObservationErrors.collectAsStateWithLifecycle()
    val taskErrorActions by viewModel.coreViewModel.taskObservationErrorActions.collectAsStateWithLifecycle()
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        taskDetails?.let { taskDetails ->
            LazyColumn(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        HeaderTitle(
                            title = taskDetails.observationTitle,
                            modifier = Modifier
                                .weight(0.65f)
                                .padding(vertical = 11.dp)
                        )
                        if (taskDetails.state == ScheduleState.RUNNING)
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
                        text = taskDetails.observationType,
                        color = MoreColors.Secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    )

                    TimeframeDays(
                        taskDetails.start.jvmLocalDate(),
                        taskDetails.end.jvmLocalDate(),
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                    TimeframeHours(
                        taskDetails.start.jvmLocalDateTime(),
                        taskDetails.end.jvmLocalDateTime(),
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Accordion(
                        title = getStringResource(id = R.string.participant_information),
                        description = taskDetails.participantInformation,
                        hasCheck = false,
                        hasPreview = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                scheduleId?.let {
                    if (!taskDetails.state.completed()) {
                        DatapointCollectionView(
                            dataPoints,
                            taskDetails.state
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ObservationErrorListView(
                    errors = taskErrors,
                    errorActions = taskErrorActions
                )

                if (!taskDetails.hidden) {
                    SmallTextButton(
                        text = if (taskDetails.state == ScheduleState.RUNNING) getStringResource(
                            id = R.string.more_observation_pause
                        )
                        else if (taskDetails.observationType == QuestionType().observationType) getStringResource(
                            id = R.string.more_questionnaire_start
                        )
                        else if (taskDetails.observationType == LimeSurveyType().observationType) getStringResource(
                            id = R.string.more_limesurvey_start
                        )
                        else getStringResource(
                            id = R.string.more_observation_start
                        ),
                        enabled = taskDetails.state.active() && if (taskDetails.observationType == PolarVerityHeartRateType(
                                emptySet()
                            ).observationType
                        ) viewModel.polarHrReady.value else true
                    ) {
                        if (taskDetails.observationType == QuestionType().observationType) {
                            navController.navigate(
                                NavigationScreen.QUESTION.navigationRoute(
                                    "scheduleId" to scheduleId
                                )
                            )
                        } else if (taskDetails.observationType == LimeSurveyType().observationType) {
                            navController.navigate(
                                NavigationScreen.LIMESURVEY.navigationRoute(
                                    "scheduleId" to scheduleId
                                )
                            )
                        } else if (taskDetails.state == ScheduleState.RUNNING) {
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