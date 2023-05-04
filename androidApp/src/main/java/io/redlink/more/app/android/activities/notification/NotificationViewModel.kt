package io.redlink.more.app.android.activities.notification

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.activities.notification.filter.NotificationFilterViewModel
import io.redlink.more.more_app_mutliplatform.models.NotificationModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NotificationViewModel: ViewModel() {
    private val coreFilterViewModel: CoreNotificationFilterViewModel = CoreNotificationFilterViewModel()
    private val coreViewModel: CoreNotificationViewModel = CoreNotificationViewModel(coreFilterViewModel)
    var notificationList: MutableState<List<NotificationModel>> = mutableStateOf(emptyList())
    val notificationCount: MutableState<Int> = mutableStateOf(0)

    val filterModel = NotificationFilterViewModel(coreFilterViewModel)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.notificationList.collect {
                withContext(Dispatchers.Main) {
                    notificationList.value = it
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.count.collect{
                withContext(Dispatchers.Main) {
                    notificationCount.value = it
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
        viewModelScope.launch {
            coreViewModel.setNotificationReadStatus(notification)
        }
    }
}
