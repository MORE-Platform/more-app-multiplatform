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

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun IconInline(icon: ImageVector, color: Color = MoreColors.Primary, contentDescription: String, modifier: Modifier = Modifier) {
    Icon(
        icon,
        contentDescription = contentDescription,
        tint = color,
        modifier = Modifier
            .width(IntrinsicSize.Min)
    )
}