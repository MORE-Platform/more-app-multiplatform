package io.redlink.more.app.android.activities.info

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.shared_composables.NavigationText
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun InfoItem(title: String, imageVector: ImageVector, contentDescription: String, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(60.dp)
                .clickable { onClick() }
        ) {
            Row {
                Icon(
                    imageVector,
                    contentDescription = contentDescription,
                    tint = MoreColors.Secondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                NavigationText(text = title)
            }
            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Enter")
        }
        Divider()
    }
}