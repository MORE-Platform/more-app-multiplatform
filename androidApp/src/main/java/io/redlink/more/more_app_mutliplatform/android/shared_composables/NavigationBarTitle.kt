package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun NavigationBarTitle(text: String) {
    Text(
        text = text,
        color = MoreColors.PrimaryDark,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    )
}