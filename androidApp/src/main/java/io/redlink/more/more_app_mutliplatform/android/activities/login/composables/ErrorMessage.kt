package io.redlink.more.more_app_mutliplatform.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.extensions.color

@Composable
fun ErrorMessage(hasError: Boolean, errorMsg: String) {
    if (hasError) {
        Spacer(modifier = Modifier.height(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = errorMsg, color = color(id = R.color.more_important), textAlign = TextAlign.Center)
        }
    }
}