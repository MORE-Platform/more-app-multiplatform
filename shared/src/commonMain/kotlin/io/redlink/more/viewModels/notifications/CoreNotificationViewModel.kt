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
package io.redlink.more.viewModels.notifications

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.extensions.set
import io.redlink.more.models.NotificationModel
import io.redlink.more.services.notification.NotificationActionHandler
import io.redlink.more.services.notification.NotificationManager
import io.redlink.more.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable

class CoreNotificationViewModel(
    private val coreFilterModel: CoreNotificationFilterViewModel,
    private val notificationManager: NotificationManager,
    private val protocolReplacement: String? = null,
    private val hostReplacement: String? = null
) : CoreViewModel() {
    private val originalNotificationList = mutableListOf<NotificationModel>()
    private val _notificationList: MutableStateFlow<List<NotificationModel>> =
        MutableStateFlow(listOf())

    @NativeCoroutines
    val notificationList: StateFlow<List<NotificationModel>> = _notificationList

    init {
        launchScope {
            coreFilterModel.filters.collect {
                if (originalNotificationList.isNotEmpty()) {
                    if (coreFilterModel.filterActive()) {
                        _notificationList.set(coreFilterModel.applyFilter(originalNotificationList))
                    } else {
                        _notificationList.set(originalNotificationList.toList())
                    }
                }
            }
        }
        launchScope {
            notificationManager.repository.notification.getAllUserFacingNotifications()
                .cancellable()
                .collect {
                    originalNotificationList.clear()
                    originalNotificationList.addAll(NotificationModel.Companion.createModelsFrom(it))
                    if (originalNotificationList.isNotEmpty() && coreFilterModel.filterActive()) {
                        _notificationList.set(coreFilterModel.applyFilter(originalNotificationList))
                    } else {
                        _notificationList.set(originalNotificationList.toList())
                    }
                }
        }
    }

    fun handleNotificationAction(
        notification: NotificationModel,
        handler: ((NotificationActionHandler, String) -> Unit)
    ) {
        notificationManager.handleNotificationInteraction(
            notification,
            protocolReplacement,
            hostReplacement,
            handler
        )
    }
}