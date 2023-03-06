package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground

class DashboardView : ComponentActivity() {

    private val viewModel = DashboardViewModel()
    private val dashboardViewModel = DashboardViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoreBackground(rightCornerContent = {
                IconButton(onClick = { dashboardViewModel.openSettings(this)}) {
                    Icon(Icons.Default.Settings, contentDescription = "Open Settings")
                }
            }) {
                DashboardView(model = viewModel)
            }
        }
    }
}


@Composable
fun DashboardView(model: DashboardViewModel) {
    Text(
        text = model.studyTitle.value
    )
}