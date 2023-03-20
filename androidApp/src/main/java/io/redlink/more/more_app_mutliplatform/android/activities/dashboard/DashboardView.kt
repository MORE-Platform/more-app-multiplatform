package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.composables.DashboardHeader
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.composables.DashboardProgress
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.composables.FilterView
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.list.ScheduleListView
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource


@Composable
fun DashboardView(navController: NavController, viewModel: DashboardViewModel) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ){
        Column(modifier = Modifier.height(IntrinsicSize.Min)) {
            DashboardProgress(model = viewModel)
            FilterView()
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column {
            if (!viewModel.studyActive.value) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.8f)
                        .padding(top = 16.dp)
                ){
                    Text(text = getStringResource(id = R.string.study_not_active))
                }
            } else {
                ScheduleListView(navController, scheduleViewModel = viewModel.scheduleViewModel)
            }
        }
    }
}
