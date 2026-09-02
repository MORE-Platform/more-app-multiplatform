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
package io.redlink.more.app.android.activities.dashboard.schedule.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.formattedString
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.jvmLocalDate
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.Heading
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.app.android.shared_composables.SmallTitle
import io.redlink.more.app.android.theme.MoreColors
import io.redlink.more.database.entities.MilestoneEntity

@Composable
fun MilestoneSection(milestones: List<MilestoneEntity>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Heading(text = getStringResource(R.string.more_milestone_section_title))
        milestones.forEach { milestone ->
            MoreDivider(Modifier.fillMaxWidth())
            MilestoneListItem(milestone)
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun MilestoneListItem(milestone: MilestoneEntity) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MoreColors.Primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            SmallTitle(text = milestone.name, color = MoreColors.Primary)
        }
        BasicText(
            text = milestone.dateTime.jvmLocalDate().formattedString(),
            color = MoreColors.Secondary
        )
    }
}
