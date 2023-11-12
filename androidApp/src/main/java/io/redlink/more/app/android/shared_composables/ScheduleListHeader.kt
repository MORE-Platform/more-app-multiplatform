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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.dashboard.composables.FilterView
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarView
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarViewModel

@Composable
fun ScheduleListHeader(viewModel: ScheduleViewModel, navController: NavController, taskCompletionBarViewModel: TaskCompletionBarViewModel) {
    Column(modifier = Modifier.height(IntrinsicSize.Min)) {
        TaskCompletionBarView(taskCompletionBarViewModel)
        FilterView(navController, model = viewModel.filterModel, scheduleListType = viewModel.scheduleListType)
    }
}