package io.redlink.more.app.android.activities.runningSchedules

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.dashboard.schedule.list.ScheduleListView
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType

@Composable
fun RunningSchedulesView(viewModel: ScheduleViewModel, navController: NavController) {
    ScheduleListView(navController = navController, scheduleViewModel = viewModel, type = ScheduleListType.RUNNING)
}