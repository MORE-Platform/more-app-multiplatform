package io.redlink.more.app.android.shared_composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EmptyListView(text: String) {
    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        BasicText(text = text)
    }
}