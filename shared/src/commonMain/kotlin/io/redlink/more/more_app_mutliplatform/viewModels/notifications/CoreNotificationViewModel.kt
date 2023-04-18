package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CoreNotificationViewModel(private val coreFilterModel: CoreNotificationFilterViewModel) {
    private val notificationRepository: NotificationRepository = NotificationRepository()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    var notificationList: MutableStateFlow<List<NotificationSchema?>> = MutableStateFlow(listOf())
    var filteredNotificationList: MutableStateFlow<List<NotificationSchema?>> = MutableStateFlow(listOf())
    var count: MutableStateFlow<Long> = MutableStateFlow(0)

    init{
        scope.launch {
            notificationRepository.getAllNotifications().collect {
                notificationList.value = it
                filteredNotificationList.value = it
            }
        }
        scope.launch {
            notificationRepository.count().collect {
                count.value = it
            }
        }
        scope.launch {
            coreFilterModel.currentFilter.collect {
                filteredNotificationList.emit(coreFilterModel.applyFilter(notificationList.value))
            }
        }
    }

    fun onNotificationLoad(provideNewState: ((List<NotificationSchema?>) -> Unit)): Closeable {
        return notificationList.asClosure(provideNewState)
    }

    fun onCountLoad(provideNewState: (Long?) -> Unit) = count.asClosure(provideNewState)

    fun setNotificationReadStatus(notification: NotificationSchema) {
        scope.launch {
            notificationRepository.setNotificationReadStatus(notification.notificationId)
        }
    }
}