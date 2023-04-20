package io.redlink.more.app.android.shared_composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.formattedString
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.ui.theme.MoreColors
import java.util.*

@Composable
fun TimeframeDays(startTime: Date, endTime: Date, modifier: Modifier = Modifier){
    Row(modifier = modifier) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = getStringResource(R.string.more_table_item_icon_start_time),
            tint = MoreColors.Primary,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = startTime.let {
                val start: String = startTime.formattedString("dd.MM.yyyy")
                val end: String = endTime.formattedString("dd.MM.yyyy")
                if(start != end) {
                   "$start - $end"
                }else {
                    start
                }
            },
            color = MoreColors.Secondary
        )
    }
}