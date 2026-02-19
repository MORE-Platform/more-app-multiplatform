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
package io.redlink.umm.participant.services.notification

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.github.aakira.napier.Napier
import io.redlink.umm.participant.Shared
import io.redlink.umm.participant.database.entities.NotificationEntity
import io.redlink.umm.participant.database.entities.ScheduleEntity
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.extensions.mapQueryParams
import io.redlink.umm.participant.extensions.toNotificationEntity
import io.redlink.umm.participant.models.NotificationModel
import io.redlink.umm.participant.models.NotificationStatusType
import io.redlink.umm.participant.models.ScheduleState
import io.redlink.umm.participant.models.StudyState
import io.redlink.umm.participant.navigation.DeeplinkManager
import io.redlink.umm.participant.scopes.Scope
import io.redlink.umm.participant.services.network.NetworkService
import io.redlink.umm.participant.services.store.SharedStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface LocalNotificationListener {
    fun displayNotification(notification: NotificationEntity, badgeCount: Int = 0)

    fun clearScheduledNotifications(notifications: List<NotificationEntity>)

    fun deleteNotificationFromSystem(notificationId: String)

    fun createNewFCMToken(onCompletion: (String) -> Unit)
    fun clearNotifications()
    fun deleteFCMToken()
    fun updateBadgeCount(count: Int = 0)
}

class NotificationManager(
    val repository: MainRepository,
    private val localNotificationListener: LocalNotificationListener,
    private val networkService: NetworkService,
    private val deeplinkManager: DeeplinkManager,
    private val sharedStorageRepository: SharedStorageRepository
) {
    private val _unreadUserCount = MutableStateFlow(0)

    @NativeCoroutines
    val unreadUserCount: StateFlow<Int> = _unreadUserCount

    init {
        Scope.launch {
            repository.notification.getAllUserFacingNotifications()
                .collect { notifications ->
                    notifications.filter { !it.read }.let {
                        withContext(Dispatchers.Main) {
                            _unreadUserCount.value = it.size
                            localNotificationListener.updateBadgeCount(it.size)
                        }
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
        completed: Boolean = false,
        data: Map<String, String>? = null,
        displayNotification: Boolean
    ) {
        storeAndHandleNotification(
            shared,
            NotificationEntity.toEntity(
                notificationId = key,
                channelId = null,
                title = title,
                notificationBody = body,
                priority = priority,
                read = read,
                completed = completed,
                userFacing = title != null,
                notificationData = data
            ),
            displayNotification
        )
    }

    fun storeAndHandleNotification(
        shared: Shared,
        notification: NotificationEntity,
        displayNotification: Boolean
    ) {
        storeAndDisplayNotification(notification, displayNotification)
        if (notification.notificationData.isNotEmpty()) {
            handleNotificationDataAsync(
                shared,
                notification.getNotificationDataMap()
            )
        }
    }

    fun storeAndDisplayNotification(
        notification: NotificationEntity,
        displayNotification: Boolean
    ) {
        if (notification.title != null && notification.notificationBody != null) {
            Scope.launch(Dispatchers.IO) {
                repository.notification.storeNotification(notification)
            }
            if (displayNotification) {
                Napier.d(tag = "NotificationManager::storeAndDisplayNotification") { "Displaying notification: $notification" }
                localNotificationListener.displayNotification(notification, unreadUserCount.value)
            }
        }
    }

    fun storeNotifications(notifications: List<NotificationEntity>) {
        Scope.launch(Dispatchers.IO) {
            repository.notification.storeNotifications(notifications)
        }
    }

    fun displayNotification(notification: NotificationEntity) {
        localNotificationListener.displayNotification(notification, unreadUserCount.value)
    }

    suspend fun downloadMissedNotifications() {
        Napier.d { "Updating notifications" }
        storeNotifications(NotificationEntity.toEntityList(networkService.downloadMissedNotifications()))
    }

    fun deleteNotificationFromRepository(notificationId: String) {
        deleteNotificationFromSystemTray(notificationId)
        repository.notification.deleteNotification(notificationId)
    }

    fun deleteNotificationFromServer(msgID: String) {
        Napier.i { "Deleting notification with msgID $msgID from server..." }
        Scope.launch(Dispatchers.IO) {
            networkService.deletePushNotification(msgID)
        }
    }

    fun deleteNotificationFromSystemTray(notificationId: String) {
        localNotificationListener.deleteNotificationFromSystem(notificationId = notificationId)
    }

    fun markNotificationAsRead(notificationId: String) {
        repository.notification.setNotificationReadStatus(notificationId, true)
        deleteNotificationFromSystemTray(notificationId)
    }

    fun markNotificationAsCompleted(notificationId: String) {
        repository.notification.setNotificationCompletedStatus(notificationId, true)
        deleteNotificationFromSystemTray(notificationId)
    }

    fun handleNotificationDataAsync(shared: Shared, data: Map<String, String>) {
        Scope.launch(Dispatchers.IO) {
            handleNotificationData(
                shared,
                data
            )
        }
    }

    suspend fun handleNotificationData(
        shared: Shared,
        data: Map<String, String>
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

                val state = checkIfCompletedOrRead(deepLink).cancellable().firstOrNull()

                if (state != null) {
                    if (NotificationStatusType.READ == state) repository.notification.setNotificationReadStatus(
                        notification.notificationId,
                        true
                    )
                    if (NotificationStatusType.COMPLETED == state) repository.notification.setNotificationCompletedStatus(
                        notification.notificationId,
                        true
                    )
                }

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

    fun checkIfCompletedOrRead(
        notificationDeeplink: String,
    ): Flow<NotificationStatusType?> = flow {

        var state: NotificationStatusType? = null

        val queryParams = notificationDeeplink.mapQueryParams()
        val observationId = queryParams["observationId"]
        if (observationId.isNullOrEmpty()
            || repository.observation.observationById(observationId.first())
                .firstOrNull() == null
        ) {
            emit(null)
            return@flow
        }
        val schedule =
            repository.schedule.firstScheduleAvailableForObservationId(observationId.first())
                .cancellable().firstOrNull()

        // ScheduleState.DEACTIVATED -> ACTIVE -> PAUSE/RUNNING -> ENDED/COMPLETED
        // if the ScheduleState is DEACTIVATED and it has a repeat, the Notification will go to the next Instance of Observation
        // ACTIVE, PAUSED, RUNNING, ENDED -> is only read
        // COMPLETED gets a check
        // after Observation has ended, we don't have any means to determine anything anymore, because the Scheduler is deleted (null) from the object, so it will be set to comppleted

        state = if (schedule?.state == ScheduleState.DONE.toString() || schedule?.state == null) {
            NotificationStatusType.COMPLETED
        } else {
            NotificationStatusType.READ
        }
        emit(state)
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
        localNotificationListener.updateBadgeCount(0)
    }

    suspend fun scheduleObservationReminders(schedules: List<ScheduleEntity>) {
        val notifications = schedules.map {
            it.toNotificationEntity(true, deeplinkManager.createDeeplinkForSchedule(it))
        }
        withContext(Dispatchers.Main) {
            storeNotifications(notifications)
            notifications.forEach {
                Napier.i { "Scheduling notification ${it.notificationId} at ${it.timestamp} with deeplink: ${it.deepLink}" }
                displayNotification(it)
            }
        }
    }

    suspend fun rescheduleNotifications(notifications: List<NotificationEntity>) {
        withContext(Dispatchers.Main) {
            notifications.forEach {
                displayNotification(it)
            }
        }
    }

    suspend fun clearScheduledNotifications() {
        try {
            val notifications = repository.notification.scheduledNotifications()
            localNotificationListener.clearScheduledNotifications(notifications)
        } catch (e: Exception) {
            Napier.e { e.toString() }
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