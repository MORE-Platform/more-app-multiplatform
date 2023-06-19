package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.NotificationFilterModel
import io.redlink.more.more_app_mutliplatform.models.NotificationFilterTypeModel
import io.redlink.more.more_app_mutliplatform.models.NotificationModel
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.flow.MutableStateFlow

class CoreNotificationFilterViewModel {
    private var highPriority: Long = 1

    val currentFilter = MutableStateFlow(NotificationFilterModel())

    /**
     * Pass the String representing a filter
     * according to NotificationFilterTypeModel type field
     */
    fun processFilterChange(filter: String?) {
        Scope.launch {
//            currentFilter.emit(
////                currentFilter.value.changeFilter(filter)
//            )
        }
    }

    fun setPlatformHighPriority(priority: Long) {
        highPriority = priority
    }

    fun applyFilter(notificationList: List<NotificationModel>): List<NotificationModel> {
        var filteredList = notificationList
        if (currentFilter.value.isNotEmpty())
            filteredList = filteredList.filter { notification -> ((
                    if (currentFilter.value.contains(NotificationFilterTypeModel.IMPORTANT)) {
                        notification.priority == highPriority
                    } else
                        true
                ) && (
                    if (currentFilter.value.contains(NotificationFilterTypeModel.UNREAD)) {
                        !notification.read
                    } else true
                    ))
            }
        return filteredList
    }

    fun getEnumAsList(): List<NotificationFilterTypeModel> {
        return NotificationFilterTypeModel.values().toList()
    }

    fun onLoadCurrentFilters(provideNewState: ((NotificationFilterModel) -> Unit)): Closeable {
        return currentFilter.asClosure(provideNewState)
    }
}