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
package io.redlink.more.viewModels.notifications

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.extensions.mapState
import io.redlink.more.extensions.set
import io.redlink.more.models.NotificationFilterTypeModel
import io.redlink.more.models.NotificationModel
import io.redlink.more.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CoreNotificationFilterViewModel : CoreViewModel() {
    private var highPriority: Long = 2

    private val _filters = MutableStateFlow<Map<NotificationFilterTypeModel, Boolean>>(mapOf())

    @NativeCoroutines
    val filters: StateFlow<Map<NotificationFilterTypeModel, Boolean>> = _filters

    @NativeCoroutines
    val activeTypes: StateFlow<Set<String>> = filters.mapState(viewModelScope) {
        it.filter { it.value }.map { it.key.type }.toSet()
    }

    init {
        val map = getEnumAsList().associateWith { false }.toMutableMap()
        map[NotificationFilterTypeModel.ALL] = true
        _filters.set(map)
    }

    fun toggleFilter(filter: NotificationFilterTypeModel) {
        var filterMap = _filters.value.toMutableMap()
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
        _filters.set(filterMap)
    }

    fun setPlatformHighPriority(priority: Long) {
        highPriority = priority
    }

    fun applyFilter(notificationList: List<NotificationModel>): List<NotificationModel> {
        return if (filterActive()) {
            notificationList.filter { notification ->
                if (_filters.value[NotificationFilterTypeModel.IMPORTANT] == true) {
                    notification.priority == highPriority
                } else {
                    true
                } && if (_filters.value[NotificationFilterTypeModel.UNREAD] == true) {
                    !notification.read
                } else {
                    true
                }
            }
        } else notificationList
    }

    fun filterActive() = _filters.value[NotificationFilterTypeModel.ALL] == false

    private fun getEnumAsList(): List<NotificationFilterTypeModel> {
        return NotificationFilterTypeModel.entries
    }
}