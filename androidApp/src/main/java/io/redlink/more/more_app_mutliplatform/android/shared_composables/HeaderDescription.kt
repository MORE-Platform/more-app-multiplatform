package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors


@Composable
fun HeaderDescription (description: String, color: Color = MoreColors.Primary) {
    Text(
        text = description,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = color
    )
}