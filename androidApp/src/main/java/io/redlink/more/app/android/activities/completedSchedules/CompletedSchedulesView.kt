package io.redlink.more.app.android.activities.completedSchedules

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.dashboard.schedule.list.ScheduleListView
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType

@Composable
fun CompletedSchedulesView(viewModel: ScheduleViewModel, navController: NavController) {
    ScheduleListView(navController = navController, scheduleViewModel = viewModel, type = ScheduleListType.COMPLETED, showButton = false)
}