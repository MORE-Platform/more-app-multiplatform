package io.redlink.more.app.android.shared_composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun HeaderTitle (
    title: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = MoreColors.Primary,
    maxLines: Int = 3,
) {
    Text(
        text = title,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = color,
        textAlign = textAlign,
        modifier = modifier
    )
}