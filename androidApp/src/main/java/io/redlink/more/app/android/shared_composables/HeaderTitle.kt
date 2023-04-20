package io.redlink.more.app.android.shared_composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun HeaderTitle (title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = MoreColors.Primary,
        modifier = modifier
    )
}