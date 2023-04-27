package io.redlink.more.app.android.activities.notification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.notification.composables.NotificationFilterViewButton
import io.redlink.more.app.android.activities.notification.composables.NotificationItem
import io.redlink.more.app.android.extensions.getStringResource


@Composable
fun NotificationView(navController: NavController, viewModel: NotificationViewModel) {

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.9f)
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
            if (viewModel.notificationCount.value.toInt() == 0) {
                Text(text = getStringResource(id = R.string.no_notifications_yet))
            }
        }

        items(viewModel.notificationList.value) { notification ->
            if (notification?.title != null) {
                Column(
                    modifier = Modifier.clickable {
                        viewModel.setNotificationToRead(notification)
                    }
                ) {
                    NotificationItem(
                        title = notification.title,
                        body = notification.notificationBody,
                        read = notification.read,
                        priority = notification.priority
                    )
                }
            }
        }
    }
}