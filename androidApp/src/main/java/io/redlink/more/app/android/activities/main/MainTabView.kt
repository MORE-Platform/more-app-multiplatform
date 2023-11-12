/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.activities.main

import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.main.composables.TabItem


@Composable
fun MainTabView(selectedIndex: Int, onTabChange: (Int) -> Unit) {
    val nameSet = setOf(NavigationScreen.DASHBOARD.stringRes(), NavigationScreen.NOTIFICATIONS.stringRes(), NavigationScreen.INFO.stringRes())
    TabRow(
        selectedTabIndex = 0,
        backgroundColor = Color.Transparent,
        indicator = {
            TabRowDefaults.Indicator(
                color = Color.Transparent
            )
        },
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