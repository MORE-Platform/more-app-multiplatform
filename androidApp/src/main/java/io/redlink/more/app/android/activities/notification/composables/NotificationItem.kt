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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.notification.NotificationViewModel
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.extensions.formattedString
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.jvmLocalDateTimeFromEpochSeconds
import io.redlink.more.app.android.extensions.toAnnotatedString
import io.redlink.more.app.android.shared_composables.IconInline
import io.redlink.more.app.android.theme.MoreColors
import io.redlink.more.models.NotificationModel
import io.redlink.more.models.localize
import io.redlink.umm.blendedcare.app.android.R

@Composable
fun NotificationItem(
    viewModel: NotificationViewModel,
    notificationModel: NotificationModel,
    navController: NavController
) {
    val context = LocalContext.current
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
                        color = if (it.toInt() == 2) MoreColors.Companion.Important else MoreColors.Companion.Primary,
                        modifier = Modifier.fillMaxWidth(0.96f)
                    )
                }
            }

            if (!notificationModel.read) {
                IconInline(
                    icon = Icons.Filled.Circle,
                    color = MoreColors.Companion.Important,
                    contentDescription = getStringResource(id = R.string.more_notification_view_show_unread),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 50.dp)
        ) {
            val localizedBody = remember(notificationModel.notificationBody) {
                notificationModel.notificationBody.trim().localize()
                    .trim().toAnnotatedString()
            }
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxHeight()
            ) {
                ClickableText(
                    text = localizedBody,
                    onClick = { offset ->
                        localizedBody.getStringAnnotations(
                            tag = "URL",
                            start = offset,
                            end = offset
                        )
                            .firstOrNull()?.let { annotation ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                context.startActivity(intent)
                            } ?: run {
                            if (!notificationModel.read) {
                                viewModel.handleNotificationAction(notificationModel, navController)
                            }
                        }
                    }
                )

                Text(
                    text = notificationModel.timestamp.jvmLocalDateTimeFromEpochSeconds()
                        .formattedString("dd.MM.yyyy HH:mm:ss"),
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = MoreColors.Companion.Secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (notificationModel.deepLink != null) {
                if (!notificationModel.read || notificationModel.completed) {
                    Icon(
                        if (notificationModel.completed) Icons.Default.Done else Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = getStringResource(id = R.string.more_observation_open),
                        tint = if (notificationModel.read) MoreColors.Companion.Approved else MoreColors.Companion.Primary
                    )
                }
            }
        }
    }
}