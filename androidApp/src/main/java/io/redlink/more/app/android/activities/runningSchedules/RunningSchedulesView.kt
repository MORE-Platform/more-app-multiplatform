package io.redlink.more.app.android.activities.runningSchedules

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.dashboard.schedule.list.ScheduleListView

@Composable
fun RunningSchedulesView(viewModel: ScheduleViewModel, navController: NavController) {
    ScheduleListView(navController = navController, scheduleViewModel = viewModel, showButton = true)
}