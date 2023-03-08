package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.modules

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.observations.questionnaire.QuestionnaireActivity
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivity

@Composable
fun ScheduleView(model: DashboardViewModel) {
    val context = LocalContext.current
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