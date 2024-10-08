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
package io.redlink.more.app.android.activities.main.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Badge
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun TabItem(
    text: String,
    icon: ImageVector,
    iconDescription: String,
    selected: Boolean,
    badgeCount: Int = 0
) {
    val selectedColor = MoreColors.PrimaryDark
    val unselectedColor = MoreColors.Primary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = if (selected) selectedColor else unselectedColor)
    ) {
        Box {
            Icon(
                icon,
                contentDescription = iconDescription,
                tint = MoreColors.White
            )
            if (badgeCount > 0) {
                Badge(
                    modifier = Modifier
                        .border(1.dp, color = Color.White, shape = CircleShape)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                ) {
                    Text(text = "$badgeCount")
                }
            }
        }

        Text(
            text = text,
            color = MoreColors.White
        )
    }
}