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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun MessageAlertDialog(
    title: String,
    message: String,
    positiveButtonTitle: String,
    positiveButtonColors: ButtonColors? = null,
    negativeButtonTitle: String? = null,
    negativeButtonColors: ButtonColors? = null,
    onPositive: () -> Unit,
    onNegative: () -> Unit = {},
) {
    val defaultButtonColors = ButtonDefaults.textButtonColors(backgroundColor = MoreColors.PrimaryLight, contentColor = MoreColors.Primary)
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = title,
                color = MoreColors.Important,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = { onPositive() },
                colors = positiveButtonColors ?: defaultButtonColors
            ) {
                Text(text = positiveButtonTitle)
            }
        },
        dismissButton = {
            if (negativeButtonTitle != null) {
                TextButton(onClick = { onNegative() },
                    colors = negativeButtonColors ?: defaultButtonColors
                ) {
                    Text(text = negativeButtonTitle)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(0.8f)
    )
}