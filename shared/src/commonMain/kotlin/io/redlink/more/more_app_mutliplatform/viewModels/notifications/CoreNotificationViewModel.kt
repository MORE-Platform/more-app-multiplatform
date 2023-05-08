package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.NotificationModel
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreNotificationViewModel(private val coreFilterModel: CoreNotificationFilterViewModel): CoreViewModel() {
    private val notificationRepository: NotificationRepository = NotificationRepository()
    private val originalNotificationList = mutableListOf<NotificationModel>()
    val notificationList: MutableStateFlow<List<NotificationModel>> = MutableStateFlow(listOf())
    var count: MutableStateFlow<Int> = MutableStateFlow(0)

    override fun viewDidAppear() {
        launchScope {
            notificationRepository.getAllNotifications().collect {
                originalNotificationList.clear()
                originalNotificationList.addAll(NotificationModel.createModelsFrom(it))
                notificationList.emit(coreFilterModel.applyFilter(originalNotificationList))
                count.emit(it.size)
            }
        }
        launchScope {
            coreFilterModel.currentFilter.collect {
                notificationList.emit(coreFilterModel.applyFilter(originalNotificationList))
            }
        }
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        originalNotificationList.clear()
        viewModelScope.launch {
            notificationList.emit(emptyList())
        }
    }

    fun onNotificationLoad(provideNewState: ((List<NotificationModel>) -> Unit)): Closeable {
        return notificationList.asClosure(provideNewState)
    }

    fun onCountLoad(provideNewState: (Int?) -> Unit) = count.asClosure(provideNewState)

    fun setNotificationReadStatus(notification: NotificationModel) {
        notificationRepository.setNotificationReadStatus(notification.notificationId)
    }
}