package io.redlink.more.more_app_mutliplatform.android.activities.main

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.setting.SettingsViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.tasks.TaskDetailsViewModel
import kotlinx.coroutines.launch

class MainViewModel(context: Context): ViewModel() {

    val tabIndex = mutableStateOf(0)
    val showBackButton = mutableStateOf(false)
    val navigationBarTitle = mutableStateOf("")

    val dashboardViewModel = DashboardViewModel(context)
    val taskDetailsViewModel = TaskDetailsViewModel(dashboardViewModel.scheduleViewModel.coreViewModel)
    val settingsViewModel = SettingsViewModel()

    init {
        settingsViewModel.createCoreViewModel(context)
    }

}