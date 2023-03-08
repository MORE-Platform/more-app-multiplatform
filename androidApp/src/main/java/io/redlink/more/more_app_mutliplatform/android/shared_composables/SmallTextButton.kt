package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.ui.theme.moreDefault

@Composable
fun SmallTextButton(text: String, enabled: Boolean = true, borderStroke: BorderStroke = MoreColors.borderDefault(enabled), buttonColors: ButtonColors = ButtonDefaults.moreDefault(), onClick: (() -> Unit)){
    OutlinedButton(
        onClick = onClick,
        border = borderStroke,
        colors = buttonColors,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(vertical = 8.dp)
            .height(50.dp)
    ) {
        Text(text = text)
    }
}