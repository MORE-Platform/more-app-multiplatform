package io.redlink.more.app.android.shared_composables

import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun MoreDivider(modifier: Modifier = Modifier, thickness: Dp = 1.dp, color: Color = MoreColors.Divider) {
    Divider(modifier = modifier, thickness = thickness, color = color)
}