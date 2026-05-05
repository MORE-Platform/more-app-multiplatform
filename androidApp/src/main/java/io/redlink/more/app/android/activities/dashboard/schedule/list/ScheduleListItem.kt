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
package io.redlink.more.app.android.activities.dashboard.schedule.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.tasks.ObservationActionButton
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.getStringResourceByName
import io.redlink.more.app.android.extensions.jvmLocalDateTime
import io.redlink.more.app.android.extensions.observationTypeToResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.SmallTitle
import io.redlink.more.app.android.shared_composables.TimeframeHours
import io.redlink.more.app.android.theme.MoreColors
import io.redlink.more.models.ScheduleModel
import io.redlink.more.models.ScheduleState
import io.redlink.more.observations.observationTypes.PolarVerityHeartRateType

@Composable
fun ScheduleListItem(
    navController: NavController,
    viewModel: ScheduleViewModel,
    showButton: Boolean,
    scheduleModel: () -> ScheduleModel
) {
    val observationErrors by viewModel.coreViewModel.observationErrors.collectAsStateWithLifecycle()
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
            SmallTitle(
                text = scheduleModel().observationTitle,
                color = MoreColors.Primary
            )
            if (scheduleModel().scheduleState == ScheduleState.RUNNING) {
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
            BasicText(
                text = getStringResourceByName(scheduleModel().observationType.observationTypeToResource()),
                color = MoreColors.Secondary
            )
            Row(horizontalArrangement = Arrangement.End) {
                if ((observationErrors[scheduleModel().observationType]?.count()
                        ?: 0) > 0
                ) {
                    BasicText(text = "${observationErrors[scheduleModel().observationType]?.count() ?: 0}")
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MoreColors.Important,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = getStringResource(id = R.string.more_schedule_details),
                    tint = MoreColors.Primary
                )
            }
        }

        TimeframeHours(
            startTime = scheduleModel().start.jvmLocalDateTime(),
            endTime = scheduleModel().end.jvmLocalDateTime(),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        if (showButton && !scheduleModel().hidden) {
            ObservationActionButton(
                navController,
                scheduleModel().scheduleId,
                scheduleModel().observationType,
                scheduleModel().scheduleState,
                additionalEnableCondition = if (PolarVerityHeartRateType(
                        setOf()
                    ).matches(
                        scheduleModel().observationType
                    )
                ) viewModel.polarHrReady.value else true
            ) {
                if (scheduleModel().scheduleState == ScheduleState.RUNNING) {
                    viewModel.pauseObservation(scheduleModel().scheduleId)
                } else {
                    viewModel.startObservation(scheduleModel().scheduleId)
                }
            }
        }
    }
}