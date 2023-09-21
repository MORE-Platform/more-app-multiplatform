package io.redlink.more.app.android.activities.notification.filter

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.models.NotificationFilterTypeModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationFilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationFilterViewModel(private val coreViewModel: CoreNotificationFilterViewModel) :
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

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }

    fun toggleFilter(filter: NotificationFilterTypeModel) {
        coreViewModel.toggleFilter(filter)
    }
}