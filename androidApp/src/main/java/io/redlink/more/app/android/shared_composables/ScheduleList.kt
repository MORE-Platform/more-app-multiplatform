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
package io.redlink.more.app.android.shared_composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.dashboard.schedule.list.ScheduleListItem
import io.redlink.more.app.android.extensions.formattedString

@Composable
fun ScheduleList(viewModel: ScheduleViewModel, navController: NavController, showButton: Boolean) {
    LazyColumn {
        if (viewModel.schedulesByDate.isNotEmpty()) {
            viewModel.schedulesByDate.entries.sortedBy { it.key }.forEach { entry ->
                if (entry.value.isNotEmpty()) {
                    item {
                        Heading(
                            text = entry.key.formattedString(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    itemsIndexed(
                        entry.value.sortedWith(
                            compareBy(
                                { it.start },
                                { it.end },
                                { it.observationTitle },
                                { it.scheduleId })
                        )
                    ) { _, item ->
                        MoreDivider(Modifier.fillMaxWidth())
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        NavigationScreen.SCHEDULE_DETAILS.navigationRoute(
                                            "scheduleId" to item.scheduleId,
                                            "scheduleListType" to viewModel.scheduleListType
                                        )
                                    )
                                }
                        ) {
                            ScheduleListItem(
                                navController = navController,
                                scheduleModel = item,
                                viewModel,
                                showButton = showButton
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}