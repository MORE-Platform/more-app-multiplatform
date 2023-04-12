import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreNotificationViewModel {
    private val notificationRepository: NotificationRepository = NotificationRepository()
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    var notificationList: MutableStateFlow<List<NotificationSchema>> = MutableStateFlow(listOf())

    fun loadNotificationList() {
        scope.launch {
            notificationRepository.getAllNotifications().collect {
                notificationList.value = it
            }
        }
    }

    fun updateNotificationReadStatus(notification: NotificationSchema) {
        notificationRepository.update(notification.notificationId, true, notification.priority)
    }
}