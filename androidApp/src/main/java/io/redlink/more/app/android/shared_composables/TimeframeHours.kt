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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.formattedString
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.theme.MoreColors
import java.time.LocalDateTime

@Composable
fun TimeframeHours(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    modifier: Modifier = Modifier,
    isRunning: Boolean = false
) {
    val primaryColor = if (isRunning) MoreColors.Approved else MoreColors.Primary
    val secondaryColor = if (isRunning) MoreColors.Approved else MoreColors.Secondary
    Row(modifier = modifier) {
        Icon(
            Icons.Default.AccessTimeFilled,
            contentDescription = getStringResource(R.string.more_table_item_icon_start_time),
            tint = primaryColor,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = getStringResource(id = R.string.more_schedule_timeframe),
            color = primaryColor,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = "${startTime.formattedString("HH:mm")} - ${endTime.formattedString("HH:mm")}",
            color = secondaryColor
        )
    }
}