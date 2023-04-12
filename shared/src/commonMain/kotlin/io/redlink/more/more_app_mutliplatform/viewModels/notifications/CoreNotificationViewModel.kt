package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreNotificationViewModel {
    private val notificationRepository: NotificationRepository = NotificationRepository()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    var notificationList: MutableStateFlow<List<NotificationSchema?>> = MutableStateFlow(listOf())

    fun getNotificationList() : List<NotificationSchema> {
        var list: List<NotificationSchema> = listOf()
        scope.launch {
            notificationRepository.getAllNotifications().collect {
                notificationList.value = it
                list = it
            }
        }
        return list
    }

    fun getNotificationCount(): Long {
        var count: Long = 0
        scope.launch {
            notificationRepository.count().collect {
                count = it
            }
        }
        return count
    }

    fun updateNotificationReadStatus(notification: NotificationSchema) {
        notificationRepository.update(notification.notificationId, true, notification.priority)
    }
}