package io.redlink.more.more_app_mutliplatform.android.activities.main.composables

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getString
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun SettingsButton(onClick: () -> Unit,
                   modifier: Modifier = Modifier,
                   enabled: Boolean = true) {
    IconButton(onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(IntrinsicSize.Max)
    ) {
        Icon(
            Icons.Default.Settings,
            contentDescription = getString(R.string.more_main_settings_button_description),
            tint = MoreColors.Primary,
            modifier = modifier
        )
    }
}