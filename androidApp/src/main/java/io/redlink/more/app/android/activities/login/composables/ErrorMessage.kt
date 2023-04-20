package io.redlink.more.app.android.activities.login.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun ErrorMessage(hasError: Boolean, errorMsg: String) {
    if (hasError) {
        Spacer(modifier = Modifier.height(8.dp).fillMaxWidth())
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = errorMsg, color = MoreColors.Important, textAlign = TextAlign.Center)
        }
    }
}