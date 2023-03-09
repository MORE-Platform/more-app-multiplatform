package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun BasicText(
    text: String,
    color: Color = MoreColors.Main,
    modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = color,
        modifier = modifier
    )
}