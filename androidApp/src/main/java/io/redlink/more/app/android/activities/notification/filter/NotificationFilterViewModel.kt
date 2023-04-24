import androidx.lifecycle.ViewModel
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NotificationFilterViewModel(coreViewModel: CoreNotificationFilterViewModel): ViewModel() {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    val currentFilter = MutableStateFlow(NotificationFilterModel())

    var notificationFilterMap = mapOf(
        Pair(
            getString(R.string.more_all_notification_filter),
            null
        ),
        Pair(
            getString(R.string.more_unread_notifications),
            "unread"
        ),
        Pair(
            getString(R.string.more_important_notifications),
            "important"
        )
    )
    init {
        scope.launch {
            coreViewModel.currentFilter.collect {
                currentFilter.emit(it)
            }
        }
    }

    fun processFilter(filter: String?) {
        coreViewModel.processFilterChange(filter)
    }
}