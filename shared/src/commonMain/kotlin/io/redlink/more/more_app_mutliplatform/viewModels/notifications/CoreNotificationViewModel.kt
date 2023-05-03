package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreNotificationViewModel: CoreViewModel() {
    private val coreFilterModel: CoreNotificationFilterViewModel = CoreNotificationFilterViewModel()
    private val notificationRepository: NotificationRepository = NotificationRepository()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val originalNotificationList = mutableListOf<NotificationSchema?>()
    val notificationList: MutableStateFlow<List<NotificationSchema?>> = MutableStateFlow(listOf())
    var count: MutableStateFlow<Int> = MutableStateFlow(0)

    override fun viewDidAppear() {
        launchScope {
            notificationRepository.getAllNotifications().collect {
                originalNotificationList.clear()
                originalNotificationList.addAll(it)
                notificationList.emit(it)
                count.emit(it.size)
            }
        }
        launchScope {
            coreFilterModel.currentFilter.collect {
                notificationList.emit(coreFilterModel.applyFilter(originalNotificationList))
            }
        }
    }

    fun onNotificationLoad(provideNewState: ((List<NotificationSchema?>) -> Unit)): Closeable {
        return notificationList.asClosure(provideNewState)
    }

    fun onCountLoad(provideNewState: (Int?) -> Unit) = count.asClosure(provideNewState)

    fun setNotificationReadStatus(notification: NotificationSchema) {
        scope.launch {
            notificationRepository.setNotificationReadStatus(notification.notificationId)
        }
    }


}