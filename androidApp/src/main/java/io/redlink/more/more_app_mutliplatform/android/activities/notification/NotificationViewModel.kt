package io.redlink.more.more_app_mutliplatform.android.activities.studyDetails

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.time.Instant
import java.util.Date

data class Notification (
    val title: String,
    val body: String,
    val read: Boolean,
    val isImportant: Boolean
)


class NotificationViewModel: ViewModel() {
    private val coreViewModel: CoreNotificationViewModel = CoreNotificationViewModel()
    var notificationList: MutableState<List<NotificationSchema?>> = mutableStateOf(listOf())
    var notificationCount: MutableState<Long> = mutableStateOf(0)

    private val scope = CoroutineScope(Dispatchers.Default + Job())

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
