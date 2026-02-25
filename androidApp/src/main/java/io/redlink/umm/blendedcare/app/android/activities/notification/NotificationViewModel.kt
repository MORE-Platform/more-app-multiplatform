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
package io.redlink.umm.blendedcare.app.android.activities.notification

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import io.redlink.umm.blendedcare.app.android.MoreApplication
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.extensions.applicationId
import io.redlink.umm.blendedcare.app.android.extensions.stringResource
import io.redlink.umm.participant.models.NotificationModel
import io.redlink.umm.participant.services.notification.NotificationActionHandler
import io.redlink.umm.participant.viewModels.notifications.CoreNotificationFilterViewModel
import io.redlink.umm.participant.viewModels.notifications.CoreNotificationViewModel

class NotificationViewModel(private val coreFilterViewModel: CoreNotificationFilterViewModel) :
    ViewModel() {
    val coreViewModel: CoreNotificationViewModel =
        CoreNotificationViewModel(
            coreFilterViewModel,
            MoreApplication.shared!!.notificationManager,
            stringResource(R.string.app_scheme),
            applicationId
        )

    fun handleNotificationAction(notification: NotificationModel, navController: NavController) {
        coreViewModel.handleNotificationAction(notification) { actionType, data ->
            when (actionType) {
                NotificationActionHandler.DEEPLINK -> navController.navigate(data.toUri())
            }
        }
    }

    fun getFilterString(): String {
        if (!coreFilterViewModel.filterActive()) {
            return stringResource(R.string.more_filter_notification_all)
        }
        return coreFilterViewModel.activeTypes.value.joinToString(", ")
    }
}
