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

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.applicationId
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.more_app_mutliplatform.models.NotificationModel
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationActionHandler
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NotificationViewModel(private val coreFilterViewModel: CoreNotificationFilterViewModel) :
    ViewModel() {
    private val coreViewModel: CoreNotificationViewModel =
        CoreNotificationViewModel(
            coreFilterViewModel,
            MoreApplication.shared!!.notificationManager,
            stringResource(R.string.app_scheme),
            applicationId
        )
    val notificationList = mutableStateListOf<NotificationModel>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.notificationList.collect {
                withContext(Dispatchers.Main) {
                    notificationList.clear()
                    notificationList.addAll(it)
                }
            }
        }
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }

    fun handleNotificationAction(notification: NotificationModel, navController: NavController) {
        coreViewModel.handleNotificationAction(notification) { actionType, data ->
            when (actionType) {
                NotificationActionHandler.DEEPLINK -> navController.navigate(Uri.parse(data))
            }
        }
    }

    fun getFilterString(): String {
        if (!coreFilterViewModel.filterActive()) {
            return stringResource(R.string.more_filter_notification_all)
        }
        return coreFilterViewModel.getActiveTypes().joinToString(", ")
    }
}
