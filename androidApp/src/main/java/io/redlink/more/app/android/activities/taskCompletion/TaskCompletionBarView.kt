package io.redlink.more.app.android.activities.taskCompletion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.ActivityProgressView

@Composable
fun TaskCompletionBarView(viewModel: TaskCompletionBarViewModel) {
    Column(modifier = Modifier
        .fillMaxWidth()
    ) {
        ActivityProgressView(
            finishedTasks = viewModel.taskCompletion.value.finishedTasks,
            totalTasks = viewModel.taskCompletion.value.totalTasks,
            headline = getStringResource(id = R.string.more_main_overall_progress)
        )
    }
}