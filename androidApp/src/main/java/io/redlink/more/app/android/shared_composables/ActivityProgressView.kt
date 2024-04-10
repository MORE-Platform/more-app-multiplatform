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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.R


@Composable
fun ActivityProgressView(modifier: Modifier = Modifier, finishedTasks: Int, totalTasks: Int, headline: String = getStringResource(id = R.string.more_main_completed_tasks)){
    val percent: Double = if(totalTasks > 0) finishedTasks.toDouble() / totalTasks.toDouble() else 0.0
    Column(
        modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Text(
                text = headline,
                color = MoreColors.Secondary,
                maxLines = 1,
                modifier = Modifier.weight(0.8f)
            )
            Text(
                text = String.format("%.2f%%", percent * 100),
                color = MoreColors.Primary,
                maxLines = 1,
            )
        }

        LinearProgressIndicator(
            progress = percent.toFloat(),
            color = MoreColors.Primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .padding(2.dp)
                .clip(RoundedCornerShape(20))
        )
    }
}