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
) {
    Text(
        text = text,
        fontWeight = FontWeight.Medium,
        fontSize = fontSize,
        color = color,
        textAlign = textAlign,
        modifier = modifier
    )
}