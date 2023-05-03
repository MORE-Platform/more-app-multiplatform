package io.redlink.more.app.android.activities.main

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.bluetooth_conntection_view.BluetoothConnectionViewModel
import io.redlink.more.app.android.activities.leaveStudy.LeaveStudyViewModel
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireViewModel
import io.redlink.more.app.android.activities.setting.SettingsViewModel
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsViewModel
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarViewModel
import io.redlink.more.app.android.activities.tasks.ObservationDetailsViewModel
import io.redlink.more.app.android.activities.tasks.TaskDetailsViewModel
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.app.android.workers.ScheduleUpdateWorker
import io.redlink.more.app.android.activities.notification.NotificationViewModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.database.repository.BluetoothDeviceRepository
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion.SimpleQuestionCoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.redlink.more.app.android.activities.dashboard.DashboardViewModel
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.services.ObservationRecordingService
import io.redlink.more.app.android.services.bluetooth.AndroidBluetoothConnector

class MainViewModel(context: Context): ViewModel() {
    private val bluetoothConnector = AndroidBluetoothConnector(context)
    private val recorder: AndroidDataRecorder by lazy { AndroidDataRecorder() }
    val tabIndex = mutableStateOf(0)
    val showBackButton = mutableStateOf(false)
    val navigationBarTitle = mutableStateOf("")

    val notificationViewModel = NotificationViewModel()
    val allSchedulesViewModel = ScheduleViewModel(CoreDashboardFilterViewModel(), recorder, ScheduleListType.ALL)
    val runningSchedulesViewModel: ScheduleViewModel by lazy {
        ScheduleViewModel(
            CoreDashboardFilterViewModel(),
            recorder,
            ScheduleListType.RUNNING
        )
    }
    val completedSchedulesViewModel: ScheduleViewModel by lazy {
        ScheduleViewModel(
            CoreDashboardFilterViewModel(),
            recorder,
            ScheduleListType.COMPLETED
        )
    }
    val dashboardViewModel = DashboardViewModel(allSchedulesViewModel)
    val settingsViewModel: SettingsViewModel by lazy { SettingsViewModel() }
    val studyDetailsViewModel: StudyDetailsViewModel by lazy { StudyDetailsViewModel() }
    val leaveStudyViewModel: LeaveStudyViewModel by lazy { LeaveStudyViewModel() }

    val taskCompletionBarViewModel = TaskCompletionBarViewModel()

    val bluetoothConnectionViewModel: BluetoothConnectionViewModel by lazy {
        BluetoothConnectionViewModel(bluetoothConnector)
    }

    fun createNewTaskViewModel(scheduleId: String) = TaskDetailsViewModel(scheduleId, recorder)
    init {
        viewModelScope.launch(Dispatchers.IO) {
            BluetoothDeviceRepository(bluetoothConnector).updateConnectedDevices()
            if (!ObservationRecordingService.running) {
                val workManager = WorkManager.getInstance(context)
                val worker = OneTimeWorkRequestBuilder<ScheduleUpdateWorker>().build()
                workManager.enqueueUniqueWork(
                    ScheduleUpdateWorker.WORKER_TAG,
                    ExistingWorkPolicy.KEEP,
                    worker)
            }
        }
    }

    fun viewDidAppear() {
        recorder.restartAll()
    }

    fun creteNewSimpleQuestionViewModel(scheduleId: String): QuestionnaireViewModel {
        return QuestionnaireViewModel(
            SimpleQuestionCoreViewModel(scheduleId, MoreApplication.observationFactory!!)
        )
    }

    fun createObservationDetailView(observationId: String): ObservationDetailsViewModel {
       return ObservationDetailsViewModel(observationId)
    }
}