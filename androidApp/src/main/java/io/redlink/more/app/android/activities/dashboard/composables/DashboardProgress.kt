package io.redlink.more.app.android.activities.dashboard.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.dashboard.DashboardViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.ActivityProgressView

@Composable
fun DashboardProgress(model: DashboardViewModel) {

    Column(modifier = Modifier
        .fillMaxWidth()
    ) {
        ActivityProgressView(
            finishedTasks = model.finishedTasks.value,
            totalTasks = model.totalTasks.value,
            headline = getStringResource(id = R.string.more_main_overall_progress)
        )
    }
}