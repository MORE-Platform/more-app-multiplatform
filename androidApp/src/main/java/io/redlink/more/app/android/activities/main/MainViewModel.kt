package io.redlink.more.app.android.activities.main

import NotificationFilterViewModel
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.redlink.more.app.android.activities.bluetooth_conntection_view.BluetoothConnectionViewModel
import io.redlink.more.app.android.activities.dashboard.DashboardViewModel
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.leaveStudy.LeaveStudyViewModel
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireViewModel
import io.redlink.more.app.android.activities.setting.SettingsViewModel
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsViewModel
import io.redlink.more.app.android.activities.tasks.ObservationDetailsViewModel
import io.redlink.more.app.android.activities.tasks.TaskDetailsViewModel
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.app.android.services.bluetooth.AndroidBluetoothConnector
import io.redlink.more.app.android.workers.ScheduleUpdateWorker
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.database.repository.BluetoothDeviceRepository
import io.redlink.more.app.android.activities.notification.NotificationViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion.SimpleQuestionCoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(context: Context): ViewModel() {
    private val bluetoothConnector = AndroidBluetoothConnector(context)
    val recorder: AndroidDataRecorder by lazy { AndroidDataRecorder(context) }
    val tabIndex = mutableStateOf(0)
    val showBackButton = mutableStateOf(false)
    val navigationBarTitle = mutableStateOf("")

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
    val dashboardViewModel = DashboardViewModel(context, allSchedulesViewModel)
    val settingsViewModel: SettingsViewModel by lazy { SettingsViewModel(context) }
    val studyDetailsViewModel: StudyDetailsViewModel by lazy { StudyDetailsViewModel() }
    val leaveStudyViewModel: LeaveStudyViewModel by lazy { LeaveStudyViewModel(context) }

    val bluetoothConnectionViewModel: BluetoothConnectionViewModel by lazy {
        BluetoothConnectionViewModel(bluetoothConnector)
    }

    val notificationViewModel = NotificationViewModel(
        NotificationFilterViewModel(CoreNotificationFilterViewModel())
    )

    fun createNewTaskViewModel(scheduleId: String) = TaskDetailsViewModel(scheduleId, recorder)
    init {
        viewModelScope.launch(Dispatchers.IO) {
            BluetoothDeviceRepository(bluetoothConnector).updateConnectedDevices()
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