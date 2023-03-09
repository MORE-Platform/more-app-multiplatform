package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun MediumTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        color = MoreColors.Main,
        modifier = modifier
    )
}