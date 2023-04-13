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
    var count: MutableStateFlow<Long> = MutableStateFlow(0)

    init{
        scope.launch {
            notificationRepository.getAllNotifications().collect {
                notificationList.value = it
            }
        }
        scope.launch {
            notificationRepository.count().collect {
                count.value = it
            }
        }
    }


    fun setNotificationReadStatus(notification: NotificationSchema) {
        notificationRepository.setNotificationReadStatus(notification.notificationId)

        scope.launch {
            notificationRepository.getAllNotifications().collect {
                notificationList.value = it
            }
        }
    }
}