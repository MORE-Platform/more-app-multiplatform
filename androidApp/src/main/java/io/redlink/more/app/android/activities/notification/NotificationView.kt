package io.redlink.more.app.android.activities.notification

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.notification.composables.NotificationFilterViewButton
import io.redlink.more.app.android.activities.notification.composables.NotificationItem
import io.redlink.more.app.android.extensions.getStringResource


@Composable
fun NotificationView(navController: NavController, viewModel: NotificationViewModel) {
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route =
        backStackEntry?.arguments?.getString(NavigationScreen.NOTIFICATIONS.routeWithParameters())
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }
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
            if (viewModel.notificationList.isEmpty()) {
                Text(text = getStringResource(id = R.string.no_notifications_yet))
            }
        }

        items(viewModel.notificationList.sortedByDescending { it.timestamp }) { notification ->
            Column(
                modifier = Modifier.clickable {
                    if (!notification.read) {
                        notification.deepLink?.let {
                            navController.navigate(Uri.parse(it))
                        } ?: run {
                            viewModel.setNotificationToRead(notification)
                        }
                    }
                }
            ) {
                NotificationItem(
                    notification
                )
            }
        }
    }
}