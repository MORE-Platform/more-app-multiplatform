package io.redlink.more.app.android.shared_composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.BuildConfig
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun AppVersion() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = "${getStringResource(R.string.app_version)}: ${BuildConfig.VERSION_NAME}",
            fontSize = 10.sp,
            color = MoreColors.Primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
@Preview
fun AppVersionPreview() {
    AppVersion()
}