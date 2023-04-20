package io.redlink.more.app.android.activities.info

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.shared_composables.BasicText

@Composable
fun InfoView(navController: NavController, viewModel: InfoViewModel) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Divider()
            InfoItem(
                title = "Study Details",
                imageVector = Icons.Default.Info,
                contentDescription = "Open Study Details",
                onClick = {
                    navController.navigate(NavigationScreen.STUDY_DETAILS.route)
                }
            )
            InfoItem(
                title = "Running Observations",
                imageVector = Icons.Outlined.Circle,
                contentDescription = "Open Settings",
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.route)
                }
            )
            InfoItem(
                title = "Completed Observations",
                imageVector = Icons.Default.Check,
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
            InfoItem(
                title = "Leave Study",
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Exit Application",
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.route)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.padding())

        }
    }
}
