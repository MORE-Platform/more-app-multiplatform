package io.redlink.more.app.android.activities.notification

import io.redlink.more.app.android.activities.notification.filter.NotificationFilterViewModel
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NotificationViewModel(coreFilterViewModel: CoreNotificationFilterViewModel): ViewModel() {
    private val coreViewModel: CoreNotificationViewModel = CoreNotificationViewModel()
    var notificationList: MutableState<List<NotificationSchema?>> = mutableStateOf(emptyList())
    val notificationCount: MutableState<Long> = mutableStateOf(0)

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

    fun setNotificationToRead(notification: NotificationSchema) {
        viewModelScope.launch {
            coreViewModel.setNotificationReadStatus(notification)
        }
    }
}
