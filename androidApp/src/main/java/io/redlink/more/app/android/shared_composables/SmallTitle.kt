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

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun SmallTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MoreColors.Primary,
    textAlign: TextAlign? = null,
    fontSize: TextUnit = 16.sp,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        fontWeight = FontWeight.Medium,
        fontSize = fontSize,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        modifier = modifier
    )
}