package io.redlink.more.app.android.activities.notification.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.realm.kotlin.internal.interop.Timestamp
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.formattedString
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.jvmLocalDateTime
import io.redlink.more.app.android.extensions.jvmLocalDateTimeFromMilliseconds
import io.redlink.more.app.android.shared_composables.IconInline
import io.redlink.more.more_app_mutliplatform.extensions.toLocalDateTime

@Composable
fun NotificationItem(
    title: String?,
    titleColor: Color = MoreColors.Primary,
    body: String?,
    read: Boolean?,
    priority: Long?,
    timestamp: Long?
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                priority?.let {
                    if (it.toInt() == 2) {
                        Image(
                            id = R.drawable.warning_exclamation,
                            contentDescription = getStringResource(id = R.string.more_logo),
                            modifier = Modifier
                                .fillMaxWidth(0.06f)
                                .padding(top = 4.dp)
                        )
                        Spacer(
                            modifier = if (read != null && read) Modifier.width(5.dp) else Modifier.width(
                                8.dp
                            )
                        )
                    }
                    Text(
                        text = title ?: getStringResource(id = R.string.notification),
                        fontWeight = if (read != null && read) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (it.toInt() == 2) MoreColors.Important else titleColor,
                        modifier = Modifier.fillMaxWidth(0.96f)
                    )
                }
            }

            if (read == false) {
                IconInline(
                    icon = Icons.Filled.Circle,
                    color = MoreColors.Important,
                    contentDescription = getStringResource(id = R.string.more_notification_view_show_unread),
                )
            }
        }

        Divider()

        body?.let {
            Text(
                text = it,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = MoreColors.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
            )
        }

        timestamp?.let {
            Text(
                text = it.jvmLocalDateTimeFromMilliseconds().formattedString("dd.MM.yyyy hh:mm"),
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = MoreColors.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
            )
        }
    }
}