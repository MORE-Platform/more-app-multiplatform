package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.material.Text
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground

class DashboardView : ComponentActivity() {

    private val viewModel = DashboardViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoreBackground {
                DashboardView(model = viewModel)
            }
        }
    }
}

@Composable
fun DashboardView(model: DashboardViewModel) {
    Text(
        text = model.study.value.studyTitle
    )
}