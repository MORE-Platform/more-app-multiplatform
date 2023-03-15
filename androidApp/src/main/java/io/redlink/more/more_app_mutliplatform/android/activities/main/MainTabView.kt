package io.redlink.more.more_app_mutliplatform.android.activities.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun MainTabView(selectedIndex: Int, onTabChange: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = 0,
        modifier = Modifier.fillMaxSize()
    ) {
        Tab(selected = selectedIndex == 0,
            onClick = {
                onTabChange(0)
            }) {
            Icon(Icons.Default.Home, contentDescription = "Home View")
            Text(text = "Dashboard")
        }
        Tab(selected = selectedIndex == 1,
            onClick = {
                onTabChange(1)
            }) {
            Icon(Icons.Default.Notifications, contentDescription = "Home View")
            Text(text = "Notifications")
        }
        Tab(selected = selectedIndex == 2,
            onClick = {
                onTabChange(2)
            }) {
            Icon(Icons.Default.Info, contentDescription = "Notifcation View")
            Text(text = "Info")
        }
    }
}