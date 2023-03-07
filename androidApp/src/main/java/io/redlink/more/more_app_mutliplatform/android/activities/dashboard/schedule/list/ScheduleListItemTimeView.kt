package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.formattedString
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import java.time.LocalTime
import java.util.Date

@Composable
fun ScheduleListItemTimeView(startTime: Date, activeFor: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = getStringResource(R.string.more_table_item_icon_start_time),
                tint = MoreColors.Main
            )
            Text(
                text = "${getStringResource(id = R.string.more_schedule_view_start_time)} ${startTime.formattedString("HH:mm")}",
                color = MoreColors.Main
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timelapse,
                contentDescription = getStringResource(R.string.more_table_item_icon_duration),
                tint = MoreColors.Main
            )
            Text(
                text = String.format(getStringResource(id = R.string.more_schedule_view_active_for), activeFor),
                color = MoreColors.Main
            )
        }
    }
}