package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.ui.theme.morePrimary

@Composable
fun SmallTextIconButton(
    text: String,
    imageText: String,
    image: ImageVector,
    imageTint: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderStroke: BorderStroke = MoreColors.borderPrimary(enabled),
    buttonColors: ButtonColors = ButtonDefaults.morePrimary(),
    onClick: (() -> Unit)
) {
    OutlinedButton(
        onClick = onClick,
        border = borderStroke,
        colors = buttonColors,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth(0.35f)
    ) {
        Icon(
            imageVector = image,
            contentDescription = imageText,
            tint = imageTint,
            modifier = modifier
        )
        Text(text = text)
    }
}