package io.redlink.more.more_app_mutliplatform.android.activities.studyDetails

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.*
import java.time.Instant
import java.util.Date

data class Notification (
    val title: String,
    val message: String,
    val read: Boolean,
    val isImportant: Boolean
)


class NotificationViewModel: ViewModel() {
    //private val coreViewModel = CoreNotificationViewModel()
    var currentFilter = "All Notifications"

    val notificationList: List<Notification> = listOf(
        Notification(
            title = "Observation Accelerometer Test is ready to start",
            message = "",
            read = false,
            isImportant = false
        ),
        Notification(
            title = "Triggered Intervention Name",
            message = "",
            read = false,
            isImportant = true
        ),
        Notification(
            title = "Task on observation Distance Walked will end soon",
            message = "The timeframe for the Task on Distance Walked has nearly ended. Please be sure to complete it in time.",
            read = false,
            isImportant = true
        ),
        Notification(
            title = "Task on observation XXX will end soon",
            message = "",
            read = true,
            isImportant = true
        ),
        Notification(
            title = "Start Questionnaire is ready to start",
            message = "",
            read = true,
            isImportant = false
        ),
        Notification(
            title = "Task on observation Distance Walked will end soon",
            message = "The timeframe for the Task on Distance Walked has nearly ended. Please be sure to complete it in time.",
            read = true,
            isImportant = true
        ),
        Notification(
            title = "Task on observation Distance Walked will end soon",
            message = "The timeframe for the Task on Distance Walked has nearly ended. Please be sure to complete it in time.",
            read = true,
            isImportant = true
        ),
        Notification(
            title = "Task on observation Distance Walked will end soon",
            message = "The timeframe for the Task on Distance Walked has nearly ended. Please be sure to complete it in time.",
            read = true,
            isImportant = true
        )
    )
    /*init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.studyModel.collect{
                withContext(Dispatchers.Main) {
                    model.value = it
                }
            }
        }
    }*/
}