package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.formattedString
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import java.util.*

@Composable
fun ScheduleListItemTimeView(startTime: Date, endTime: Date) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        Icon(
            Icons.Default.AccessTimeFilled,
            contentDescription = getStringResource(R.string.more_table_item_icon_start_time),
            tint = MoreColors.Primary,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = "Timeframe: ",
            color = MoreColors.Primary
        )
        Text(
            text = "${startTime.formattedString("HH:mm")} - ${endTime.formattedString("HH:mm")}",
            color = MoreColors.Secondary
        )
    }
}