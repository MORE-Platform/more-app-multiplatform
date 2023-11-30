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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.morePrimary

@Composable
fun SmallTextIconButton(
    text: String,
    imageText: String,
    image: ImageVector,
    imageTint: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderStroke: BorderStroke = MoreColors.borderPrimary(enabled),
    buttonColors: ButtonColors = ButtonDefaults.morePrimary(),
    onClick: (() -> Unit)
) {
    OutlinedButton(
        onClick = onClick,
        border = borderStroke,
        colors = buttonColors,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth(0.4f)
    ) {
        Icon(
            imageVector = image,
            contentDescription = imageText,
            tint = imageTint,
            modifier = modifier
        )
        Text(text = text)
    }
}