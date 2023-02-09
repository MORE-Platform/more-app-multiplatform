package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

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
    val defaultButtonColors = ButtonDefaults.textButtonColors(backgroundColor = MoreColors.MainBackground, contentColor = MoreColors.Main)
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