package io.redlink.more.app.android.shared_composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun BasicText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MoreColors.Primary,
    textAlign: TextAlign? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    Text(
        text = text,
        color = color,
        textAlign = textAlign,
        fontSize = fontSize,
        modifier = modifier
    )
}