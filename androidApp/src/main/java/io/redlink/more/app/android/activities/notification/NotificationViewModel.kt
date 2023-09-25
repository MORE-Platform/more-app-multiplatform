package io.redlink.more.app.android.activities.notification

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.more_app_mutliplatform.models.NotificationModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NotificationViewModel(private val coreFilterViewModel: CoreNotificationFilterViewModel) :
    ViewModel() {
    private val coreViewModel: CoreNotificationViewModel =
        CoreNotificationViewModel(coreFilterViewModel)
    val notificationList = mutableStateListOf<NotificationModel>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.notificationList.collect {
                withContext(Dispatchers.Main) {
                    notificationList.clear()
                    notificationList.addAll(it)
                }
            }
        }
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }

    fun setNotificationToRead(notification: NotificationModel) {
        coreViewModel.setNotificationReadStatus(notification)
    }

    fun deleteNotification(notification: NotificationModel) {
        coreViewModel.deleteNotification(notificationId = notification.notificationId)
    }

    fun deleteNotification(notificationId: String) {
        coreViewModel.deleteNotification(notificationId)
    }

    fun getFilterString(): String {
        if (!coreFilterViewModel.filterActive()) {
            return stringResource(R.string.more_filter_notification_all)
        }
        return coreFilterViewModel.getActiveTypes().joinToString(", ")
    }
}
