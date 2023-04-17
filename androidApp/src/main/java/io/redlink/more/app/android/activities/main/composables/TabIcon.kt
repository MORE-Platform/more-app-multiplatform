package io.redlink.more.app.android.activities.main.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun TabItem(text: String, icon: ImageVector, iconDescription: String, selected: Boolean) {
    val selectedColor = MoreColors.PrimaryDark
    val unselectedColor = MoreColors.Primary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().background(color = if(selected) selectedColor else unselectedColor)
    ) {
        Icon(
            icon,
            contentDescription = iconDescription,
            tint = MoreColors.White
        )
        Text(
            text = text,
            color = MoreColors.White
        )
    }
}