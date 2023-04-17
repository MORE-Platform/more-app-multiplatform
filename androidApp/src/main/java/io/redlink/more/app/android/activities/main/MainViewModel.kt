package io.redlink.more.app.android.activities.main

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.app.android.activities.dashboard.DashboardViewModel
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireViewModel
import io.redlink.more.app.android.activities.setting.SettingsViewModel
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsViewModel
import io.redlink.more.app.android.activities.tasks.TaskDetailsViewModel
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion.SimpleQuestionCoreViewModel

class MainViewModel(context: Context): ViewModel() {
    private val recorder = AndroidDataRecorder(context)
    val tabIndex = mutableStateOf(0)
    val showBackButton = mutableStateOf(false)
    val navigationBarTitle = mutableStateOf("")

    val dashboardFilterViewModel = CoreDashboardFilterViewModel()
    val dashboardViewModel = DashboardViewModel(context, recorder, dashboardFilterViewModel)
    val settingsViewModel = SettingsViewModel(context)
    val studyDetailsViewModel = StudyDetailsViewModel()

    fun createNewTaskViewModel(scheduleId: String) = TaskDetailsViewModel(scheduleId, recorder)

    fun creteNewSimpleQuestionViewModel(scheduleId: String, context: Context): QuestionnaireViewModel {
        return QuestionnaireViewModel(
            SimpleQuestionCoreViewModel(scheduleId, AndroidObservationFactory(context))
        )
    }
}