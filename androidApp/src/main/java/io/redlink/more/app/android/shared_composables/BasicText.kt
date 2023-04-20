package io.redlink.more.app.android.shared_composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun BasicText(
    text: String,
    color: Color = MoreColors.Primary,
    modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = color,
        modifier = modifier
    )
}