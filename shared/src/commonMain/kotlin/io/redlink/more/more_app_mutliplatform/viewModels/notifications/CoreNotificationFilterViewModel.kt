/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.more_app_mutliplatform.viewModels.notifications

import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.models.NotificationFilterTypeModel
import io.redlink.more.more_app_mutliplatform.models.NotificationModel
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class CoreNotificationFilterViewModel : CoreViewModel() {
    private var highPriority: Long = 2

    val filters = MutableStateFlow<Map<NotificationFilterTypeModel, Boolean>>(mapOf())

    init {
        val map = getEnumAsList().associateWith { false }.toMutableMap()
        map[NotificationFilterTypeModel.ALL] = true
        filters.set(map)
    }

    fun toggleFilter(filter: NotificationFilterTypeModel) {
        var filterMap = filters.value.toMutableMap()
        if (filter == NotificationFilterTypeModel.ALL) {
            filterMap = filterMap.mapValues { false }.toMutableMap()
            filterMap[NotificationFilterTypeModel.ALL] = true
        } else if (filterMap[NotificationFilterTypeModel.ALL] == true) {
            filterMap[NotificationFilterTypeModel.ALL] = false
            filterMap[filter] = true
        } else if (filterMap[filter] == true) {
            if (filterMap.values.filter { it }.size == 1) {
                filterMap = filterMap.mapValues { false }.toMutableMap()
                filterMap[NotificationFilterTypeModel.ALL] = true
            } else {
                filterMap[filter] = false
            }
        } else {
            filterMap[filter] = true
        }
        filters.set(filterMap)
    }

    override fun viewDidAppear() {

    }

    fun setPlatformHighPriority(priority: Long) {
        highPriority = priority
    }

    fun applyFilter(notificationList: List<NotificationModel>): List<NotificationModel> {
        return if (filterActive()) {
            notificationList.filter { notification ->
                if (filters.value[NotificationFilterTypeModel.IMPORTANT] == true) {
                    notification.priority == highPriority
                } else {
                    true
                } && if (filters.value[NotificationFilterTypeModel.UNREAD] == true) {
                    !notification.read
                } else {
                    true
                }
            }
        } else notificationList
    }

    fun filterActive() = filters.value[NotificationFilterTypeModel.ALL] == false

    private fun getEnumAsList(): List<NotificationFilterTypeModel> {
        return NotificationFilterTypeModel.entries
    }

    fun getActiveTypes() = filters.value.filter { it.value }.map { it.key.type }.toSet()

    fun onFilterChange(provideNewstate: (Map<NotificationFilterTypeModel, Boolean>) -> Unit) =
        filters.asClosure(provideNewstate)
}