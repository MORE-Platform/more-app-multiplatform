package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.models.NotificationModel
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable

class CoreNotificationViewModel(private val coreFilterModel: CoreNotificationFilterViewModel, private val notificationRepository: NotificationRepository): CoreViewModel() {
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
            notificationRepository.getAllUserFacingNotifications().cancellable().collect {
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
        notificationRepository.setNotificationReadStatus(notification.notificationId)
        if (notification.deepLink != null) {
            deleteNotification(notification.notificationId)
        }
    }

    fun deleteNotification(notificationId: String) {
        notificationRepository.deleteNotification(notificationId)
    }
}