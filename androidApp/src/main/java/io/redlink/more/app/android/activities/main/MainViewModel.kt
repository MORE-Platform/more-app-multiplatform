package io.redlink.more.app.android.activities.main

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.bluetooth_conntection_view.BluetoothConnectionViewModel
import io.redlink.more.app.android.activities.dashboard.DashboardViewModel
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.info.InfoViewModel
import io.redlink.more.app.android.activities.leaveStudy.LeaveStudyViewModel
import io.redlink.more.app.android.activities.loginBLESetup.LoginBLESetupActivity
import io.redlink.more.app.android.activities.notification.NotificationViewModel
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireViewModel
import io.redlink.more.app.android.activities.setting.SettingsViewModel
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsViewModel
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarViewModel
import io.redlink.more.app.android.activities.studyDetails.observationDetails.ObservationDetailsViewModel
import io.redlink.more.app.android.activities.tasks.TaskDetailsViewModel
import io.redlink.more.app.android.extensions.showNewActivity
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.database.repository.BluetoothDeviceRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(context: Context) : ViewModel() {
    private val recorder: AndroidDataRecorder by lazy { AndroidDataRecorder() }
    val tabIndex = mutableStateOf(0)
    val showBackButton = mutableStateOf(false)
    val navigationBarTitle = mutableStateOf("")

    val notificationViewModel = NotificationViewModel()
    val allSchedulesViewModel =
        ScheduleViewModel(CoreDashboardFilterViewModel(), recorder, ScheduleListType.ALL)
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
        BluetoothConnectionViewModel(MoreApplication.androidBluetoothConnector!!)
    }

    val infoVM: InfoViewModel by lazy {
        InfoViewModel()
    }

    private val simpleQuestionnaireViewModel by lazy {
        QuestionnaireViewModel()
    }

    private val taskDetailsViewModel: TaskDetailsViewModel by lazy {
        TaskDetailsViewModel(recorder)
    }

    init {
        recorder.restartAll()
        MoreApplication.shared!!.showBleSetup(MoreApplication.observationFactory!!) {
            if (it) {
                openBLESetupActivity(context)
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    BluetoothDeviceRepository(MoreApplication.androidBluetoothConnector).updateConnectedDevices()
                }
            }
        }
    }

    fun getTaskDetailsVM(scheduleId: String) =
        taskDetailsViewModel.apply { setSchedule(scheduleId) }

    fun viewDidAppear() {

    }

    fun creteNewSimpleQuestionViewModel(scheduleId: String): QuestionnaireViewModel {
        return simpleQuestionnaireViewModel.apply { setScheduleId(scheduleId) }
    }

    fun createObservationDetailView(observationId: String): ObservationDetailsViewModel {
        return ObservationDetailsViewModel(observationId)
    }

    private fun openBLESetupActivity(context: Context) {
        (context as? Activity)?.let {
            showNewActivity(it, LoginBLESetupActivity::class.java)
        }
    }
}