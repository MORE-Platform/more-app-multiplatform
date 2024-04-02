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
package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.models.NotificationModel
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable

class CoreNotificationViewModel(
    private val coreFilterModel: CoreNotificationFilterViewModel,
    private val notificationManager: NotificationManager,
    private val protocolReplacement: String? = null,
    private val hostReplacement: String? = null
) : CoreViewModel() {
    private val originalNotificationList = mutableListOf<NotificationModel>()
    val notificationList: MutableStateFlow<List<NotificationModel>> = MutableStateFlow(listOf())

    override fun viewDidAppear() {
        launchScope {
            coreFilterModel.filters.collect {
                if (originalNotificationList.isNotEmpty()) {
                    if (coreFilterModel.filterActive()) {
                        notificationList.set(coreFilterModel.applyFilter(originalNotificationList))
                    } else {
                        notificationList.set(originalNotificationList.toList())
                    }
                }
            }
        }
        launchScope {
            notificationManager.notificationRepository.getAllUserFacingNotifications().cancellable()
                .collect {
                    originalNotificationList.clear()
                    originalNotificationList.addAll(NotificationModel.createModelsFrom(it))
                    if (originalNotificationList.isNotEmpty() && coreFilterModel.filterActive()) {
                        notificationList.set(coreFilterModel.applyFilter(originalNotificationList))
                    } else {
                        notificationList.set(originalNotificationList.toList())
                    }
                }
        }
    }

    fun onNotificationLoad(provideNewState: ((List<NotificationModel>) -> Unit)): Closeable {
        return notificationList.asClosure(provideNewState)
    }

    fun setNotificationReadStatus(notification: NotificationModel) {
        if (!notification.read) {
            notificationManager.markNotificationAsRead(notification.notificationId)
        }
    }

    fun handleNotificationAction(
        notification: NotificationModel,
        deepLinkHandler: ((String) -> Unit)
    ) {
        notificationManager.handleNotificationInteraction(
            notification,
            protocolReplacement,
            hostReplacement,
            deepLinkHandler
        )
    }
}