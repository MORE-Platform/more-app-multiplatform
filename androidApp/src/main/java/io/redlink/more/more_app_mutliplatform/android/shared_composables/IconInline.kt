package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun IconInline(icon: ImageVector, color: Color = MoreColors.Primary, contentDescription: String, modifier: Modifier = Modifier) {
    Icon(
        icon,
        contentDescription = contentDescription,
        tint = color,
        modifier = Modifier
            .width(IntrinsicSize.Min)
    )
}