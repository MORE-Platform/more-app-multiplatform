package io.redlink.more.more_app_mutliplatform.android.activities.main

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.observations.questionnaire.QuestionnaireViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.setting.SettingsViewModel
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.tasks.ObservationDetailsViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.tasks.TaskDetailsViewModel
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidDataRecorder
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.android.workers.ScheduleUpdateWorker
import io.redlink.more.app.android.services.bluetooth.AndroidBluetoothConnector
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion.SimpleQuestionCoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    init {
        viewModelScope.launch(Dispatchers.IO) {
            recorder.updateTaskStates()
            val workManager = WorkManager.getInstance(context)
            val worker = OneTimeWorkRequestBuilder<ScheduleUpdateWorker>().build()
            workManager.enqueueUniqueWork(
                ScheduleUpdateWorker.WORKER_TAG,
                ExistingWorkPolicy.KEEP,
                worker)
        }
    }

    fun creteNewSimpleQuestionViewModel(scheduleId: String, context: Context): QuestionnaireViewModel {
        return QuestionnaireViewModel(
            SimpleQuestionCoreViewModel(scheduleId, AndroidObservationFactory(context))
        )
    }

    fun createObservationDetailView(observationId: String): ObservationDetailsViewModel {
       return ObservationDetailsViewModel(observationId)
    }
}