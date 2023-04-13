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
    private val coreNotificationViewModel: CoreNotificationViewModel = CoreNotificationViewModel()
    var notifications: MutableState<List<NotificationSchema?>> = mutableStateOf(listOf(
        NotificationSchema()
    ))
    var notificationCount: MutableState<Long> = mutableStateOf(0)

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    var currentFilter = "All Notifications"

    val notificationList: List<Notification> = listOf(
        Notification(
            title = "Observation Accelerometer Test is ready to start",
            body = "",
            read = false,
            isImportant = false
        ),
        Notification(
            title = "Triggered Intervention Name",
            body = "",
            read = false,
            isImportant = true
        ),
        Notification(
            title = "Task on observation Distance Walked will end soon",
            body = "The timeframe for the Task on Distance Walked has nearly ended. Please be sure to complete it in time.",
            read = false,
            isImportant = true
        ),
        Notification(
            title = "Task on observation XXX will end soon",
            body = "",
            read = true,
            isImportant = true
        ),
        Notification(
            title = "Start Questionnaire is ready to start",
            body = "",
            read = true,
            isImportant = false
        ),
        Notification(
            title = "Task on observation Distance Walked will end soon",
            body = "The timeframe for the Task on Distance Walked has nearly ended. Please be sure to complete it in time.",
            read = true,
            isImportant = true
        ),
        Notification(
            title = "Task on observation Distance Walked will end soon",
            body = "The timeframe for the Task on Distance Walked has nearly ended. Please be sure to complete it in time.",
            read = true,
            isImportant = true
        ),
        Notification(
            title = "Task on observation Distance Walked will end soon",
            body = "The timeframe for the Task on Distance Walked has nearly ended. Please be sure to complete it in time.",
            read = true,
            isImportant = true
        )
    )
    init {
        viewModelScope.launch(Dispatchers.IO) {
            scope.launch {
                coreNotificationViewModel.notificationList.collect{
                    notifications.value = it
                }
            }
            scope.launch {
                coreNotificationViewModel.count.collect{
                    notificationCount.value = it
                }
            }
        }
    }
}