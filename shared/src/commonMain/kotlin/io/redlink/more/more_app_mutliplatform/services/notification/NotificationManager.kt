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
package io.redlink.more.more_app_mutliplatform.services.notification

import io.github.aakira.napier.Napier
import io.realm.kotlin.ext.toRealmDictionary
import io.realm.kotlin.types.RealmDictionary
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.models.NotificationModel
import io.redlink.more.more_app_mutliplatform.models.StudyState
import io.redlink.more.more_app_mutliplatform.navigation.DeeplinkManager
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

interface LocalNotificationListener {
    fun displayNotification(notification: NotificationSchema)

    fun deleteNotificationFromSystem(notificationId: String)

    fun createNewFCMToken(onCompletion: (String) -> Unit)
    fun clearNotifications()
    fun deleteFCMToken()
    fun updateBadgeCount(count: Int = 0)
}

class NotificationManager(
    private val localNotificationListener: LocalNotificationListener,
    private val networkService: NetworkService,
    private val deeplinkManager: DeeplinkManager,
    private val sharedStorageRepository: SharedStorageRepository
) {
    val notificationRepository = NotificationRepository()
    val _unreadUserCount = MutableStateFlow(0)
    val unreadUserCount: StateFlow<Int> = _unreadUserCount

    init {
        Scope.launch(Dispatchers.IO) {
            notificationRepository.getUnreadUserNotifications().collect { notificationList ->
                _unreadUserCount.update { notificationList.count() }
                withContext(Dispatchers.Main) {
                    localNotificationListener.updateBadgeCount(notificationList.count())
                }
            }
        }
    }

    fun storeAndHandleNotification(
        shared: Shared,
        key: String,
        title: String?,
        body: String?,
        priority: Long = 1,
        read: Boolean = false,
        data: Map<String, String>? = null,
        displayNotification: Boolean
    ) {
        storeAndHandleNotification(
            shared,
            NotificationSchema.toSchema(
                notificationId = key,
                channelId = null,
                title = title,
                notificationBody = body,
                priority = priority,
                read = read,
                userFacing = title != null,
                notificationData = data
            ),
            displayNotification
        )
    }

    fun storeAndHandleNotification(
        shared: Shared,
        notification: NotificationSchema,
        displayNotification: Boolean
    ) {
        storeAndDisplayNotification(notification, displayNotification)
        if (notification.notificationData.isNotEmpty()) {
            handleNotificationDataAsync(
                shared,
                notification.notificationData
            )
        }
    }

    fun storeAndDisplayNotification(
        notification: NotificationSchema,
        displayNotification: Boolean
    ) {
        if (notification.title != null && notification.notificationBody != null) {
            notificationRepository.storeNotification(notification)
            if (displayNotification) {
                Napier.d(tag = "NotificationManager::storeAndDisplayNotification") { "Displaying notification: $notification" }
                localNotificationListener.displayNotification(notification)
            }
        }
    }

    fun storeNotifications(notifications: List<NotificationSchema>) {
        notificationRepository.storeNotifications(notifications)
    }

    fun downloadMissedNotifications() {
        Scope.launch {
            Napier.d { "Updating notifications" }
            storeNotifications(NotificationSchema.toSchemaList(networkService.downloadMissedNotifications()))
        }
    }

    fun deleteNotificationFromRepository(notificationId: String) {
        deleteNotificationFromSystemTray(notificationId)
        notificationRepository.deleteNotification(notificationId)
    }

    fun deleteNotificationFromServer(msgID: String) {
        Napier.i { "Deleting notification with msgID $msgID from server..." }
        Scope.launch {
            networkService.deletePushNotification(msgID)
        }
    }

    fun deleteNotificationFromSystemTray(notificationId: String) {
        localNotificationListener.deleteNotificationFromSystem(notificationId = notificationId)
    }

    fun markNotificationAsRead(notificationId: String) {
        notificationRepository.setNotificationReadStatus(notificationId, true)
        deleteNotificationFromSystemTray(notificationId)
    }

    fun handleNotificationDataAsync(shared: Shared, data: Map<String, String>) {
        Scope.launch {
            handleNotificationData(
                shared,
                data.toRealmDictionary()
            )
        }
    }

    suspend fun handleNotificationData(
        shared: Shared,
        data: RealmDictionary<String>
    ) {
        if (data.isNotEmpty()) {
            if (data[MAIN_DATA_KEY] == STUDY_CHANGED) {
                updateStudy(shared, data)
            }
            data[MSG_ID]?.let {
                deleteNotificationFromServer(it)
            }
        }
    }

    fun handleNotificationInteraction(
        notificationId: String,
        deeplink: String? = null
    ) {
        if (deeplink == null || deeplink.contains(DeeplinkManager.TASK_DETAILS) || deeplink.contains(
                DeeplinkManager.OBSERVATION_DETAILS
            )
        ) {
            markNotificationAsRead(notificationId)
        }
    }

    fun handleNotificationInteraction(
        notification: NotificationModel,
        protocolReplacement: String? = null,
        hostReplacement: String? = null,
        handler: ((NotificationActionHandler, String) -> Unit)
    ) {
        notification.deepLink?.let { deepLink ->
            Scope.launch {
                deeplinkManager.modifyDeepLink(deepLink, protocolReplacement, hostReplacement)
                    .firstOrNull()?.let { modifiedDeepLink ->
                        if (modifiedDeepLink.contains(DeeplinkManager.TASK_DETAILS) || modifiedDeepLink.contains(
                                DeeplinkManager.OBSERVATION_DETAILS
                            )
                        ) {
                            withContext(Dispatchers.Main) {
                                markNotificationAsRead(notification.notificationId)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            handler(NotificationActionHandler.DEEPLINK, modifiedDeepLink)
                        }
                    } ?: run {
                    withContext(Dispatchers.Main) {
                        markNotificationAsRead(notification.notificationId)
                    }
                }
            }
        } ?: run {
            markNotificationAsRead(notification.notificationId)
        }
    }

    fun newFCMToken(token: String? = null) {
        sharedStorageRepository.remove(FCM_TOKEN_UPLOADED)
        token?.let { storeAndUploadToken(it) }
            ?: run {
                localNotificationListener.createNewFCMToken { storeAndUploadToken(it) }
            }
    }

    private fun storeAndUploadToken(newToken: String) {
        Scope.launch(Dispatchers.IO) {
            val (successful, _) = networkService.sendNotificationToken(newToken)
            sharedStorageRepository.store(FCM_TOKEN_UPLOADED, successful)
        }
    }

    fun deleteFCMToken() {
        sharedStorageRepository.remove(FCM_TOKEN_UPLOADED)
        localNotificationListener.deleteFCMToken()
    }

    fun createNewFCMIfNecessary() {
        if (!sharedStorageRepository.load(FCM_TOKEN_UPLOADED, false)) {
            newFCMToken()
        }
    }

    fun clearAllNotifications() {
        localNotificationListener.clearNotifications()
    }

    fun updateNotificationBadgeCount() {
        Scope.launch {
            notificationRepository.getUnreadUserNotifications().firstOrNull().let {
                withContext(Dispatchers.Main) {
                    localNotificationListener.updateBadgeCount(it?.count() ?: 0)
                }
            }
        }
    }

    private suspend fun updateStudy(shared: Shared, data: Map<String, String>) {
        val oldStudyState =
            data[STUDY_OLD_STATE]?.let { StudyState.getState(it) }
        val newStudyState =
            data[STUDY_NEW_STATE]?.let { StudyState.getState(it) }
        shared.updateStudy(oldStudyState, newStudyState)
    }

    companion object {
        const val FCM_TOKEN = "FCM_TOKEN"

        private const val MAIN_DATA_KEY = "key"
        private const val STUDY_CHANGED = "STUDY_STATE_CHANGED"
        private const val STUDY_OLD_STATE = "oldState"
        private const val STUDY_NEW_STATE = "newState"

        const val FCM_TOKEN_UPLOADED = "FCM_TOKEN_UPLOADED"

        const val DEEP_LINK = "deepLink"
        const val MSG_ID = "MSG_ID"
    }
}