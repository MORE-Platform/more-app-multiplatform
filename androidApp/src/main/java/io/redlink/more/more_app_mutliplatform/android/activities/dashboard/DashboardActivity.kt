package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
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
                DashboardView()
            }
        }
    }
}

@Composable
fun DashboardView() {
    Text(text = "Studytitle")
}
