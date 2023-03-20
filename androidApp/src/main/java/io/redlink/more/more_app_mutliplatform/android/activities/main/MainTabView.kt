package io.redlink.more.more_app_mutliplatform.android.activities.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.redlink.more.more_app_mutliplatform.android.activities.NavigationScreen
import io.redlink.more.more_app_mutliplatform.android.activities.main.composables.TabItem
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors


@Composable
fun MainTabView(selectedIndex: Int, onTabChange: (Int) -> Unit) {
    val selectedColor = MoreColors.PrimaryDark
    val unselectedColor = MoreColors.Primary
    val nameSet = setOf(NavigationScreen.DASHBOARD.stringRes(), NavigationScreen.NOTIFICATIONS.stringRes(), NavigationScreen.INFO.stringRes())
    TabRow(
        selectedTabIndex = 0,
        backgroundColor = Color.Transparent,
        indicator = {
            TabRowDefaults.Indicator(
                color = Color.Transparent
            )
        },
        modifier = Modifier.()
    ) {

        Tab(selected = selectedIndex == 0,
            onClick = {
                onTabChange(0)
            }) {
            val name = nameSet.elementAt(0)
            TabItem(
                text = name,
                icon = Icons.Default.Home,
                iconDescription = name,
                selected = selectedIndex == 0
            )
        }
        Tab(selected = selectedIndex == 1,
            onClick = {
                onTabChange(1)
            }) {
            val name = nameSet.elementAt(1)
            TabItem(
                text = name,
                icon = Icons.Default.Notifications,
                iconDescription = name,
                selected = selectedIndex == 1
            )
        }
        Tab(selected = selectedIndex == 2,
            onClick = {
                onTabChange(2)
            }) {
            val name = nameSet.elementAt(2)
            TabItem(
                text = name,
                icon = Icons.Default.Info,
                iconDescription = name,
                selected = selectedIndex == 2
            )
        }
    }
}