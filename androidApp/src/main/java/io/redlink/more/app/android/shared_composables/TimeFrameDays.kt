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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.formattedString
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.ui.theme.MoreColors
import java.time.LocalDate

@Composable
fun TimeframeDays(startTime: LocalDate, endTime: LocalDate, modifier: Modifier = Modifier){
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