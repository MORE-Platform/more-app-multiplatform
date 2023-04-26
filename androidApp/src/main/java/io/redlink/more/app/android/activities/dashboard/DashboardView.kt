package io.redlink.more.app.android.activities.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.dashboard.schedule.list.ScheduleListView
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.R
import io.redlink.more.app.android.shared_composables.ScheduleListHeader


@Composable
fun DashboardView(navController: NavController, viewModel: DashboardViewModel) {
//    PolarHeartRateObservation.scanForDevices()
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ){
        ScheduleListHeader(
            viewModel = viewModel.scheduleViewModel,
            navController = navController
        )
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
                    Text(text = getStringResource(id = R.string.more_dashboard_study_not_active))
                }
            } else {
                ScheduleListView(navController, scheduleViewModel = viewModel.scheduleViewModel, showButton = true)
            }
        }
    }
}
