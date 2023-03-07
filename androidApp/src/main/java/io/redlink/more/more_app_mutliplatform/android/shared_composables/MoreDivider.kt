package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun MoreDivider(modifier: Modifier = Modifier) {
    Divider(modifier = modifier, thickness = 1.dp, color = MoreColors.Divider)
}