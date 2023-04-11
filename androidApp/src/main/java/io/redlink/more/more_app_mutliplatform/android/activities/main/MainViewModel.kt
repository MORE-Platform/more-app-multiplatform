package io.redlink.more.more_app_mutliplatform.android.activities.main

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.setting.SettingsViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.NotificationViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.StudyDetailsViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.launch
import io.redlink.more.more_app_mutliplatform.android.activities.tasks.TaskDetailsViewModel
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidDataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel

class MainViewModel(context: Context): ViewModel() {
    private val recorder = AndroidDataRecorder(context)
    val tabIndex = mutableStateOf(0)
    val showBackButton = mutableStateOf(false)
    val navigationBarTitle = mutableStateOf("")

    val dashboardFilterViewModel = CoreDashboardFilterViewModel()
    val dashboardViewModel = DashboardViewModel(context, recorder, dashboardFilterViewModel)
    val settingsViewModel = SettingsViewModel(context)
    val studyDetailsViewModel = StudyDetailsViewModel()

    val notificationViewModel = NotificationViewModel()

    fun createNewTaskViewModel(scheduleId: String) = TaskDetailsViewModel(scheduleId, recorder)
}