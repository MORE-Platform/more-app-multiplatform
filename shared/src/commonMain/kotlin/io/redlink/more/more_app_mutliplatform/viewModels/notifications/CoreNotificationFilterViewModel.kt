package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.models.NotificationFilterModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class CoreNotificationFilterViewModel {
    private val currentFilter = MutableStateFlow(NotificationFilterModel())

    fun hasFilters(): Boolean = (currentFilter.value.filterRead || currentFilter.value.filterUnimportant)

    fun hasReadFilter(): Boolean = currentFilter.value.filterRead

    fun hasUnimportantFilter(): Boolean = currentFilter.value.filterUnimportant

    fun changeReadFilter() {
        currentFilter.update {
            it.copy(filterRead = !it.filterRead)
        }
    }
    fun changeUnimportantFilter() {
        currentFilter.update {
            it.copy(filterUnimportant = !it.filterUnimportant)
        }
    }

    fun applyFilter(notificationList: List<NotificationSchema>): List<NotificationSchema> {
        if(hasFilters())
            notificationList.filter {(
                    if(currentFilter.value.filterUnimportant)
                        it.priority == 2L else true
                        ) && (
                    if(currentFilter.value.filterRead)
                        !it.read else true
                    )
            }
        return notificationList
    }
}