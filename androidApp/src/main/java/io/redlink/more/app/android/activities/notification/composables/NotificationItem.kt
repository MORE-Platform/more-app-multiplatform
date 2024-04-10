/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.activities.notification.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.extensions.formattedString
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.jvmLocalDateTimeFromMilliseconds
import io.redlink.more.app.android.shared_composables.IconInline
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.models.NotificationModel

@Composable
fun NotificationItem(
    notificationModel: NotificationModel
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
                notificationModel.priority.let {
                    if (it.toInt() == 2) {
                        Image(
                            id = R.drawable.warning_exclamation,
                            contentDescription = getStringResource(id = R.string.more_logo),
                            modifier = Modifier
                                .fillMaxWidth(0.06f)
                                .padding(top = 4.dp)
                        )
                        Spacer(
                            modifier = if (notificationModel.read) Modifier.width(5.dp) else Modifier.width(
                                8.dp
                            )
                        )
                    }
                    Text(
                        text = notificationModel.title,
                        fontWeight = if (notificationModel.read) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (it.toInt() == 2) MoreColors.Important else MoreColors.Primary,
                        modifier = Modifier.fillMaxWidth(0.96f)
                    )
                }
            }

            if (!notificationModel.read) {
                IconInline(
                    icon = Icons.Filled.Circle,
                    color = MoreColors.Important,
                    contentDescription = getStringResource(id = R.string.more_notification_view_show_unread),
                )
            }
        }

        Divider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 65.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxHeight()) {
                Text(
                    text = notificationModel.notificationBody,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = MoreColors.Secondary,
                )

                Text(
                    text = notificationModel.timestamp.jvmLocalDateTimeFromMilliseconds()
                        .formattedString("dd.MM.yyyy HH:mm:ss"),
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = MoreColors.Secondary,
                )
            }
            if (notificationModel.deepLink != null) {
                Icon(
                    if(notificationModel.read) Icons.Default.Done else Icons.Default.ArrowForwardIos,
                    contentDescription = getStringResource(id = R.string.more_observation_open),
                    tint = if (notificationModel.read) MoreColors.Approved else MoreColors.Primary
                )
            }
        }
    }
}