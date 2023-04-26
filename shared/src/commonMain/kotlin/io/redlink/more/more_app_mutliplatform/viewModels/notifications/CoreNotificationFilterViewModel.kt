package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.NotificationFilterModel
import io.redlink.more.more_app_mutliplatform.models.NotificationFilterTypeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreNotificationFilterViewModel {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private var highPriority: Long = 1

    val currentFilter = MutableStateFlow(NotificationFilterModel())

    /**
     * Pass the String representing a filter
     * according to NotificationFilterTypeModel type field
     */
    fun processFilterChange(filter: String) {
        scope.launch {
            currentFilter.emit(
                currentFilter.value.changeFilter(filter)
            )
        }
    }

    fun setPlatformHighPriority(priority: Long) {
        highPriority = priority
    }

    fun applyFilter(notificationList: List<NotificationSchema?>): List<NotificationSchema?> {
        val filteredList = notificationList.toMutableList()
        if(currentFilter.value.isNotEmpty())
            filteredList.filter {
                it?.let { (
                            if(currentFilter.value.contains(NotificationFilterTypeModel.IMPORTANT))
                                it.priority == highPriority else true
                            ) && (
                            if(currentFilter.value.contains(NotificationFilterTypeModel.UNREAD))
                                !it.read else true
                            )
                } ?: false
            }
        return filteredList.toList()
    }

    fun getEnumAsList(): List<NotificationFilterTypeModel> {
        return NotificationFilterTypeModel.values().toList()
    }

    fun onLoadCurrentFilters(provideNewState: ((NotificationFilterModel) -> Unit)): Closeable {
        return currentFilter.asClosure(provideNewState)
    }
}