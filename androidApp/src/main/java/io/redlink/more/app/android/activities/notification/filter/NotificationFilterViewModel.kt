package io.redlink.more.app.android.activities.notification.filter

import androidx.lifecycle.ViewModel
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getString
import io.redlink.more.more_app_mutliplatform.models.NotificationFilterModel
import io.redlink.more.more_app_mutliplatform.models.NotificationFilterTypeModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationFilterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NotificationFilterViewModel: ViewModel() {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val coreViewModel = CoreNotificationFilterViewModel()

    val currentFilter = MutableStateFlow(NotificationFilterModel())

    var notificationFilterList = mapOf(
        Pair(
            getString(R.string.more_filter_notification_all),
            null
        ),
        Pair(
            getString(R.string.more_filter_notification_unread),
            NotificationFilterTypeModel.UNREAD.type
        ),
        Pair(
            getString(R.string.more_filter_notification_important),
            NotificationFilterTypeModel.IMPORTANT.type
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

    fun getFilterString(): String {
        var filterString = ""

        if(currentFilter.value.isEmpty()) {
            return getString(R.string.more_filter_notification_all)
        }
        notificationFilterList.forEach {(key, value) ->
            if(currentFilter.value.contains(value)) {
                if(filterString.isNotEmpty()) {
                    filterString += ", "
                }
                filterString += key
            }
        }
        return filterString
    }
}