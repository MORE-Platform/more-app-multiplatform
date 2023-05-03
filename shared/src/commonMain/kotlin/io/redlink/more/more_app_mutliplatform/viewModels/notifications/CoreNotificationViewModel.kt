package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.NotificationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreNotificationViewModel(private val coreFilterModel: CoreNotificationFilterViewModel) {
    private val notificationRepository: NotificationRepository = NotificationRepository()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val originalNotificationList = mutableListOf<NotificationModel>()
    val notificationList: MutableStateFlow<List<NotificationModel>> = MutableStateFlow(listOf())
    var count: MutableStateFlow<Long> = MutableStateFlow(0)

    init{
        scope.launch {
            notificationRepository.getAllNotifications().collect {
                originalNotificationList.clear()
                originalNotificationList.addAll(NotificationModel.createModelsFrom(it))
                notificationList.emit(coreFilterModel.applyFilter(originalNotificationList))
            }
        }
        scope.launch {
            notificationRepository.count().collect {
                count.value = it
            }
        }
        scope.launch {
            coreFilterModel.currentFilter.collect {
                notificationList.emit(coreFilterModel.applyFilter(originalNotificationList))
            }
        }
    }

    fun onNotificationLoad(provideNewState: ((List<NotificationModel>) -> Unit)): Closeable {
        return notificationList.asClosure(provideNewState)
    }

    fun onCountLoad(provideNewState: (Long?) -> Unit) = count.asClosure(provideNewState)

    fun setNotificationReadStatus(notification: NotificationModel) {
        scope.launch {
            notificationRepository.setNotificationReadStatus(notification.notificationId)
        }
    }
}