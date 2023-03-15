package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.more_app_mutliplatform.android.activities.NavigationScreen
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.formattedString
import io.redlink.more.more_app_mutliplatform.android.shared_composables.Heading
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreDivider

@Composable
fun ScheduleListView(navController: NavController, scheduleViewModel: ScheduleViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            if (scheduleViewModel.schedules.isNotEmpty()) {
                scheduleViewModel.schedules.toSortedMap().let { schedules ->
                    schedules.keys.forEach { date ->
                        item {
                            Heading(
                                text = date.formattedString(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        itemsIndexed(schedules[date] ?: emptyList()) { _, item ->
                            MoreDivider(Modifier.fillMaxWidth())
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("${NavigationScreen.SCHEDULE_DETAILS.route}/id=${item.scheduleId}")
                                    }
                            ) {
                                ScheduleListItem(scheduleModel = item, scheduleViewModel)
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
}