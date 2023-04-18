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
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import java.time.LocalDate
import java.util.*

@Composable
fun ScheduleList(schedules: SortedMap<LocalDate, List<ScheduleModel>>, viewModel: ScheduleViewModel, navController: NavController, running: Boolean) {
    LazyColumn {
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
                            navController.navigate("${NavigationScreen.SCHEDULE_DETAILS.route}/observationTitle=${item.observationTitle}&scheduleId=${item.scheduleId}")
                        }
                ) {
                    ScheduleListItem(navController = navController, scheduleModel = item, viewModel, running = running)
                }
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

    }
}