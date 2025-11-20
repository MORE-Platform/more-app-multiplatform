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
package io.redlink.umm.blendedcare.app.android.activities.completedSchedules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.umm.blendedcare.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.umm.blendedcare.app.android.activities.dashboard.schedule.list.ScheduleListView
import io.redlink.umm.blendedcare.app.android.activities.taskCompletion.TaskCompletionBarViewModel
import io.redlink.umm.blendedcare.app.android.shared_composables.ScheduleListHeader

@Composable
fun CompletedSchedulesView(
    viewModel: ScheduleViewModel,
    navController: NavController,
    taskCompletionBarViewModel: TaskCompletionBarViewModel
) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        ScheduleListHeader(
            viewModel = viewModel,
            navController = navController,
            taskCompletionBarViewModel = taskCompletionBarViewModel
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column {
            ScheduleListView(
                navController = navController,
                scheduleViewModel = viewModel,
                showButton = false
            )
        }
    }
}