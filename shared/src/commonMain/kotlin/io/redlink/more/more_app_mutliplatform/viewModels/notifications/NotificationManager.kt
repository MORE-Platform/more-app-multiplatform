package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.github.aakira.napier.Napier
import io.realm.kotlin.ext.toRealmDictionary
import io.realm.kotlin.types.RealmDictionary
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.models.StudyState
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Dispatchers

interface LocalNotificationListener {
    fun displayNotification(notification: NotificationSchema)

    fun createNewFCMToken(onCompletion: (String) -> Unit)
    fun deleteFCMToken()
}

class NotificationManager(
    private val localNotificationListener: LocalNotificationListener,
    private val networkService: NetworkService
) {
    private val notificationRepository = NotificationRepository()

    fun storeAndHandleNotification(
        shared: Shared,
        key: String,
        title: String?,
        body: String?,
        priority: Long = 2,
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

    fun storeAndHandleNotification(shared: Shared, notification: NotificationSchema, displayNotification: Boolean) {
        storeAndDisplayNotification(notification, displayNotification)
        if (notification.notificationData.isNotEmpty()) {
            handleNotificationDataAsync(shared, notification.notificationData)
        }
    }

    fun storeAndDisplayNotification(notification: NotificationSchema, displayNotification: Boolean) {
        if (notification.title != null && notification.notificationBody != null) {
            notificationRepository.storeNotification(notification)
            if (displayNotification) {
                localNotificationListener.displayNotification(notification)
            }
        }
    }

    fun storeNotifications(notifications: List<NotificationSchema>) {
        notificationRepository.storeNotifications(notifications)
    }

    fun downloadMissedNotifications() {
        Scope.launch {
            storeNotifications(NotificationSchema.toSchemaList(networkService.downloadMissedNotifications()))
        }
    }

    fun deleteNotificationFromServer(msgID: String) {
        Napier.i { "Deleting notification with msgID $msgID from server..." }
        Scope.launch {
            networkService.deletePushNotification(msgID)
        }
    }

    fun handleNotificationDataAsync(shared: Shared, data: Map<String, String>) {
        Scope.launch {
            handleNotificationData(shared, data.toRealmDictionary())
        }
    }

    suspend fun handleNotificationData(shared: Shared, data: RealmDictionary<String>) {
        if (data.isNotEmpty()) {
            if (data[MAIN_DATA_KEY] == STUDY_CHANGED) {
                updateStudy(shared, data)
            }
            data[MSG_ID]?.let {
                deleteNotificationFromServer(it)
            }
        }
    }

    fun newFCMToken(token: String? = null) {
        token?.let { storeAndUploadToken(it) }
            ?: kotlin.run {
                localNotificationListener.createNewFCMToken { storeAndUploadToken(it) }
            }
    }

    private fun storeAndUploadToken(newToken: String) {
        Scope.launch(Dispatchers.Default) {
            networkService.sendNotificationToken(newToken)
        }
    }

    fun deleteFCMToken() {
        localNotificationListener.deleteFCMToken()
    }

    private suspend fun updateStudy(shared: Shared, data: Map<String, String>) {
        val oldStudyState =
            data[STUDY_OLD_STATE]?.let { StudyState.getState(it) }
        val newStudyState =
            data[STUDY_NEW_STATE]?.let { StudyState.getState(it) }
        shared.updateStudy(oldStudyState, newStudyState)
    }

    companion object {
        private const val FCM_TOKEN = "FCM_TOKEN"

        private const val MSG_ID = "MSG_ID"
        private const val MAIN_DATA_KEY = "key"
        private const val STUDY_CHANGED = "STUDY_STATE_CHANGED"

        private const val STUDY_OLD_STATE = "oldState"
        private const val STUDY_NEW_STATE = "newState"

        const val DEEP_LINK = "deepLink"
    }
}