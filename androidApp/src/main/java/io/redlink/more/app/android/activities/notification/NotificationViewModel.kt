package io.redlink.more.app.android.activities.notification

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationViewModel
import kotlinx.coroutines.*


class NotificationViewModel: ViewModel() {
    private val coreViewModel: CoreNotificationViewModel = CoreNotificationViewModel()
    var notificationList: MutableState<List<NotificationSchema?>> = mutableStateOf(listOf())
    var notificationCount: MutableState<Long> = mutableStateOf(0)

    var currentFilter = "All Notifications"

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

    fun setNotificationToRead(notification: NotificationSchema) {
        viewModelScope.launch {
            coreViewModel.setNotificationReadStatus(notification)
        }
    }
}
