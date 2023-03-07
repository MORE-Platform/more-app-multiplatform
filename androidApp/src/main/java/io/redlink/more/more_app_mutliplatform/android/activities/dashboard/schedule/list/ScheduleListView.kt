package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.formattedString
import io.redlink.more.more_app_mutliplatform.android.shared_composables.Heading
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreDivider
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import java.time.LocalDate

@Composable
fun ScheduleListView(scheduleViewModel: ScheduleViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (scheduleViewModel.schedules.isNotEmpty()) {
                scheduleViewModel.schedules.keys.forEach { date ->
                    item {
                        Heading(
                            text = date.formattedString(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    itemsIndexed(scheduleViewModel.schedules[date] ?: emptyList()) { _, item ->
                        MoreDivider(Modifier.fillMaxWidth())
                        ScheduleListItem(scheduleModel = item)
                    }
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}