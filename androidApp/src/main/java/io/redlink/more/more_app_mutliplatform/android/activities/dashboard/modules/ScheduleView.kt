package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.modules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardViewModel

@Composable
fun ScheduleView(model: DashboardViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ){
        Text(
            text = "Here you will find schedules",
            modifier = Modifier
                .fillMaxSize()
                .weight(1f, true)
                .padding(bottom = 425.dp)
        )
    }
}