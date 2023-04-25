package io.redlink.more.app.android.shared_composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.dashboard.composables.DashboardProgress
import io.redlink.more.app.android.activities.dashboard.composables.FilterView
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel

@Composable
fun ScheduleListHeader(finishedTasks: Int, totalTasks: Int, viewModel: ScheduleViewModel, navController: NavController) {
    Column(modifier = Modifier.height(IntrinsicSize.Min)) {
        DashboardProgress(finishedTasks = finishedTasks, totalTasks = totalTasks)
        FilterView(navController, model = viewModel.filterModel)
    }
}