package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.models.NotificationFilterModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreNotificationFilterViewModel {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private var highPriority: Long = 1

    val currentFilter = MutableStateFlow(NotificationFilterModel())

    fun hasFilters(): Boolean = (currentFilter.value.filterRead || currentFilter.value.filterUnimportant)

    fun hasReadFilter(): Boolean = currentFilter.value.filterRead

    fun hasUnimportantFilter(): Boolean = currentFilter.value.filterUnimportant

    fun changeReadFilter() {
        scope.launch {
            currentFilter.emit(NotificationFilterModel(
                !currentFilter.value.filterRead,
                currentFilter.value.filterUnimportant
            ))
        }
    }

    fun changeUnimportantFilter() {
        scope.launch {
            currentFilter.emit(NotificationFilterModel(
                currentFilter.value.filterRead,
                !currentFilter.value.filterUnimportant
            ))
        }
    }

    fun setPlatformHighPriority(priority: Long) {
        highPriority = priority
    }

    fun applyFilter(notificationList: List<NotificationSchema?>): List<NotificationSchema?> {
        val filteredList = notificationList.toMutableList()
        if(hasFilters())
            filteredList.filter {
                it?.let { (
                            if(currentFilter.value.filterUnimportant)
                                it.priority == highPriority else true
                            ) && (
                            if(currentFilter.value.filterRead)
                                !it.read else true
                            )
                } ?: false
            }
        return filteredList.toList()
    }
}