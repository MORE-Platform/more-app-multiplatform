package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun SmallTitle(text: String, modifier: Modifier = Modifier, color: Color = MoreColors.Primary) {
    Text(
        text = text,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = color,
        modifier = modifier
    )
}