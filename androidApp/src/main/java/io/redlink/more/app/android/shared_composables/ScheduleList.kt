/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.app.android.shared_composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.dashboard.schedule.list.ScheduleListItem
import io.redlink.more.app.android.extensions.formattedString
import io.redlink.more.app.android.extensions.jvmLocalDate
import io.redlink.more.navigation.model.NavigationRouteParameter

@Composable
fun ScheduleList(viewModel: ScheduleViewModel, navController: NavController, showButton: Boolean) {
    val schedulesByDate by viewModel.coreViewModel.schedulesByDate.collectAsStateWithLifecycle()

    val sortedScheduleEntries by remember(schedulesByDate) {
        derivedStateOf {
            schedulesByDate.entries
                .filter { it.value.isNotEmpty() }
                .sortedBy { it.key }
                .map { entry ->
                    entry.key to entry.value.sortedWith(
                        compareBy(
                            { it.start },
                            { it.end },
                            { it.observationTitle },
                            { it.scheduleId }
                        )
                    )
                }
        }
    }

    LazyColumn {
        sortedScheduleEntries.forEach { (date, schedules) ->
            item(key = "header_$date") {
                Heading(
                    text = date.jvmLocalDate().formattedString(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(
                items = schedules,
                key = { schedule -> schedule.scheduleId }
            ) { scheduleModel ->
                MoreDivider(Modifier.fillMaxWidth())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            navController.navigate(
                                NavigationScreen.SCHEDULE_DETAILS.navigationRoute(
                                    NavigationRouteParameter.SCHEDULE_ID.key to scheduleModel.scheduleId,
                                    NavigationRouteParameter.SCHEDULE_LIST_TYPE.key to viewModel.scheduleListType
                                )
                            )
                        }
                ) {
                    ScheduleListItem(
                        navController = navController,
                        scheduleModel = { scheduleModel },
                        viewModel = viewModel,
                        showButton = showButton
                    )
                }
            }

            item(key = "spacer_$date") {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}