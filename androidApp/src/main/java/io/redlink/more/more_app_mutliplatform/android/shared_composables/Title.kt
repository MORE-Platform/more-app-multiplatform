package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun Title(text: String,
          modifier: Modifier = Modifier,
          color: Color = MoreColors.MainTitle,
          textAlign: TextAlign = TextAlign.Left
) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = color,
        textAlign = textAlign,
        maxLines = 2,
        modifier = modifier.fillMaxWidth()
    )
}