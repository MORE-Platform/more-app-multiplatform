package io.redlink.more.app.android.activities.info

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen

@Composable
fun InfoView(navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Divider()
            InfoItem(
                title = "Study Details",
                imageVector = Icons.Default.Settings,
                contentDescription = "Open Study Details",
                onClick = {
                    navController.navigate(NavigationScreen.STUDY_DETAILS.route)
                }
            )
            InfoItem(
                title = "Running Schedules",
                imageVector = Icons.Default.Settings,
                contentDescription = "Go to running schedules",
                onClick = {
                    navController.navigate(NavigationScreen.RUNNING_SCHEDULES.route)
                }
            )
            InfoItem(
                title = "Completed Schedules",
                imageVector = Icons.Default.Settings,
                contentDescription = "Go to completed schedules",
                onClick = {
                    navController.navigate(NavigationScreen.COMPLETED_SCHEDULES.route)
                }
            )
            InfoItem(
                title = "Settings",
                imageVector = Icons.Default.Settings,
                contentDescription = "Open Settings",
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.route)
                }
            )
        }
    }
}
