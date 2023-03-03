package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import android.os.Bundle
import io.redlink.more.more_app_mutliplatform.android.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.composables.DashboardHeader
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.composables.DashboardTab
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.composables.ModuleView
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.composables.ScheduleView
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.ActivityProgressView
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground

class DashboardActivity: ComponentActivity() {
    private val dashboardViewModel = DashboardViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { 
            MoreBackground(rightCornerContent = {
                IconButton(onClick = { dashboardViewModel.openSettings(this) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Open Settings")
                }
            }) {
                DashboardView(dashboardViewModel)
            }
        }
    }
}

@Composable
fun DashboardView(model: DashboardViewModel) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(IntrinsicSize.Min)
    ){
        DashboardHeader(model = model)
        DashboardTab(model = model, onClick = {
            if(it == Views.SCHEDULE.tabPosition) {
                //TODO reload data source
            }
        })
        ActivityProgressView(
            finishedTasks = model.finishedTasks.collectAsState().value,
            totalTasks = model.totalTasks.collectAsState().value)
        if (!model.studyActive.value) {
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
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
            ) {
                when (model.currentTabIndex.collectAsState().value) {
                    Views.SCHEDULE.tabPosition -> ScheduleView(model = model)
                    Views.MODULES.tabPosition -> ModuleView(model = model)
                    else -> {}
                }
            }
        }
    }
}
