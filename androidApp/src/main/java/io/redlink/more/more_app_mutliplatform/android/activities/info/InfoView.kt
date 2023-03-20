package io.redlink.more.more_app_mutliplatform.android.activities.info

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.redlink.more.more_app_mutliplatform.android.activities.NavigationScreen

@Composable
fun InfoView(navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Divider()
            InfoItem(
                title = "Study Details",
                imageVector = Icons.Default.Settings,
                contentDescription = "Open Settings",
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.route)
                }
            )
            InfoItem(
                title = "Running Observations",
                imageVector = Icons.Default.Settings,
                contentDescription = "Open Settings",
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.route)
                }
            )
            InfoItem(
                title = "Completed Observations",
                imageVector = Icons.Default.Settings,
                contentDescription = "Open Settings",
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.route)
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
