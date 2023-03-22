package io.redlink.more.more_app_mutliplatform.android.activities.main

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.setting.SettingsViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.launch
import io.redlink.more.more_app_mutliplatform.android.activities.tasks.TaskDetailsViewModel
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidDataRecorder

class MainViewModel(context: Context): ViewModel() {

    val tabIndex = mutableStateOf(0)
    val showBackButton = mutableStateOf(false)
    val navigationBarTitle = mutableStateOf("")

    val dashboardViewModel = DashboardViewModel(context)
    val taskDetailsViewModel = TaskDetailsViewModel(AndroidDataRecorder(context))
    val settingsViewModel = SettingsViewModel()
    val studyDetailsViewModel = CoreStudyDetailsViewModel()

    init {
        settingsViewModel.createCoreViewModel(context)
    }

}