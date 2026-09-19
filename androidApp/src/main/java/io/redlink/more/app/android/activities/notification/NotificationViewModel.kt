/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.app.android.activities.notification

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.models.NotificationModel
import io.redlink.more.services.notification.NotificationActionHandler
import io.redlink.more.viewModels.notifications.CoreNotificationFilterViewModel
import io.redlink.more.viewModels.notifications.CoreNotificationViewModel

class NotificationViewModel(private val coreFilterViewModel: CoreNotificationFilterViewModel) :
    ViewModel() {
    val coreViewModel: CoreNotificationViewModel =
        CoreNotificationViewModel(
            coreFilterViewModel,
            MoreApplication.shared!!.notificationManager
        )

    fun handleNotificationAction(notification: NotificationModel, navController: NavController) {
        coreViewModel.handleNotificationAction(notification) { actionType, data ->
            data?.let {
                when (actionType) {
                    NotificationActionHandler.DEEPLINK -> {
                        val uri = data.route.toUri()
                        val destRoute = uri.path?.removePrefix("/") ?: uri.toString().substringBefore("?")
                        val currentRoute = navController.currentDestination?.route?.substringBefore("?")
                        if (destRoute != currentRoute) {
                            navController.navigate(uri)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun getFilterString(): String {
        if (!coreFilterViewModel.filterActive()) {
            return stringResource(R.string.more_filter_notification_all)
        }
        return coreFilterViewModel.activeTypes.value.joinToString(", ") { it }
    }
}
