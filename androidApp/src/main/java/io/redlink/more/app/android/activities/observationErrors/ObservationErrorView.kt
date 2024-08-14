package io.redlink.more.app.android.activities.observationErrors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun ObservationErrorView() {
    val viewModel = remember {
        ObservationErrorViewModel()
    }
    ObservationErrorListView(
        errors = viewModel.observationErrors,
        errorActions = viewModel.observationErrorActions
    )
}