/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.app.android.activities.notification.filter

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.models.NotificationFilterTypeModel
import io.redlink.more.viewModels.notifications.CoreNotificationFilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationFilterViewModel(val coreViewModel: CoreNotificationFilterViewModel) :
    ViewModel() {
    val currentFilters = mutableStateMapOf<NotificationFilterTypeModel, Boolean>()

    init {
        coreViewModel.setPlatformHighPriority(2)
        viewModelScope.launch {
            coreViewModel.filters.collect {
                withContext(Dispatchers.Main) {
                    currentFilters.clear()
                    currentFilters.putAll(it)
                }
            }
        }
    }

    fun toggleFilter(filter: NotificationFilterTypeModel) {
        coreViewModel.toggleFilter(filter)
    }
}