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
package io.redlink.more.app.android.shared_composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.dashboard.composables.FilterView
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarView
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.moreImportant

@Composable
fun ScheduleListHeader(
    viewModel: ScheduleViewModel,
    navController: NavController,
    taskCompletionBarViewModel: TaskCompletionBarViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        TaskCompletionBarView(taskCompletionBarViewModel)
        if (viewModel.numberOfObservationErrors() > 0) {
            Box(modifier = Modifier.padding(vertical = 4.dp)) {

                SmallTextIconButton(
                    text = "${viewModel.numberOfObservationErrors()} ${getStringResource(id = R.string.error)}",
                    imageText = "Error",
                    image = Icons.Default.Warning,
                    imageTint = MoreColors.White,
                    buttonColors = ButtonDefaults.moreImportant(),
                ) {
                    navController.navigate(NavigationScreen.OBSERVATION_ERRORS.navigationRoute())
                }
            }
        }
        FilterView(
            navController,
            model = viewModel.filterModel,
            scheduleListType = viewModel.scheduleListType
        )
    }
}