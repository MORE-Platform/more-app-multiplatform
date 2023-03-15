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
import io.redlink.more.more_app_mutliplatform.android.activities.NavigationScreen


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
            val name = NavigationScreen.DASHBOARD.stringRes()
            Icon(Icons.Default.Home, contentDescription = name)
            Text(text = name)
        }
        Tab(selected = selectedIndex == 1,
            onClick = {
                onTabChange(1)
            }) {
            val name = NavigationScreen.NOTIFICATIONS.stringRes()
            Icon(Icons.Default.Notifications, contentDescription = name)
            Text(text = name)
        }
        Tab(selected = selectedIndex == 2,
            onClick = {
                onTabChange(2)
            }) {
            val name = NavigationScreen.INFO.stringRes()
            Icon(Icons.Default.Info, contentDescription = name)
            Text(text = name)
        }
    }
}