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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.morePrimary

@Composable
fun SmallTextButton(
    text: String,
    enabled: Boolean = true,
    borderStroke: BorderStroke = MoreColors.borderPrimary(enabled),
    buttonColors: ButtonColors = ButtonDefaults.morePrimary(),
    fontSize: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier
        .fillMaxWidth(1f)
        .padding(vertical = 8.dp)
        .height(50.dp),
    onClick: (() -> Unit)
) {
    OutlinedButton(
        onClick = onClick,
        border = borderStroke,
        colors = buttonColors,
        enabled = enabled,
        modifier = modifier
    ) {
        Text(text = text, fontSize = fontSize)
    }
}