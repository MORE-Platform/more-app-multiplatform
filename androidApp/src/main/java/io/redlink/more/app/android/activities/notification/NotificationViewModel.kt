package io.redlink.more.app.android.activities.notification

import NotificationFilterViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel

data class Notification (
    val title: String,
    val message: String,
    val read: Boolean,
    val isImportant: Boolean
)


class NotificationViewModel(filterViewModel: NotificationFilterViewModel): ViewModel() {
    //private val coreViewModel = CoreNotificationViewModel()
    val model = mutableStateOf<StudyDetailsModel?>(null)
    val filterModel = filterViewModel

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