package io.redlink.more.more_app_mutliplatform.android.activities.main.composables

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.redlink.more.more_app_mutliplatform.android.extensions.color
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getString

@Composable
fun NotificationButton(onClick: () -> Unit,
                       modifier: Modifier = Modifier,
                       enabled: Boolean = true,
                       notificationCount: Int = 0
) {
    IconButton(onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        BadgedBox(
            badge = {
                if (notificationCount > 0) {
                    Badge{
                        Text(text = "$notificationCount")
                    }
                }
            },
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = getString(R.string.more_main_notification_button_description),
                tint = color(id = R.color.more_main_color),
                modifier = modifier
            )
        }
    }
}