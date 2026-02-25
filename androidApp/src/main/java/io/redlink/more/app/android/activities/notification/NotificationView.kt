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
package io.redlink.more.app.android.activities.notification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.redlink.io.more.app.android.R
import io.redlink.more.app.android.activities.notification.composables.NotificationFilterViewButton
import io.redlink.more.app.android.activities.notification.composables.NotificationItem
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.viewModels.notifications.CoreNotificationFilterViewModel

@Composable
fun NotificationView(
    navController: NavController,
    coreFilterViewModel: CoreNotificationFilterViewModel
) {
    val viewModel = remember { NotificationViewModel(coreFilterViewModel) }
    val notificationList by viewModel.coreViewModel.notificationList.collectAsStateWithLifecycle()
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {

        item {
            Column(
                modifier = Modifier
                    .height(
                        IntrinsicSize.Min
                    )
            ) {
                NotificationFilterViewButton(navController, viewModel = viewModel)
            }
            Spacer(modifier = Modifier.padding(10.dp))
        }

        item {
            if (notificationList.isEmpty()) {
                Text(text = getStringResource(id = R.string.no_notifications_yet))
            }
        }

        items(notificationList.sortedByDescending { it.timestamp }) { notification ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (!notification.read) {
                            viewModel.handleNotificationAction(notification, navController)
                        }
                    }
                    .padding(bottom = 10.dp)
            ) {
                Column {
                    NotificationItem(viewModel, notification, navController)
                    MoreDivider()
                }
            }
        }
    }
}