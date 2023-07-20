package io.redlink.more.app.android.activities.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.BLESetup.BLEConnectionActivity
import io.redlink.more.app.android.activities.dashboard.DashboardViewModel
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.info.InfoViewModel
import io.redlink.more.app.android.activities.leaveStudy.LeaveStudyViewModel
import io.redlink.more.app.android.activities.notification.NotificationViewModel
import io.redlink.more.app.android.activities.notification.filter.NotificationFilterViewModel
import io.redlink.more.app.android.activities.observations.pushButton.PushButtonViewModel
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireViewModel
import io.redlink.more.app.android.activities.setting.SettingsViewModel
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsViewModel
import io.redlink.more.app.android.activities.studyDetails.observationDetails.ObservationDetailsViewModel
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarViewModel
import io.redlink.more.app.android.activities.tasks.TaskDetailsViewModel
import io.redlink.more.more_app_mutliplatform.database.repository.BluetoothDeviceRepository
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.CoreNotificationFilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(context: Context) : ViewModel() {
    val tabIndex = mutableStateOf(0)
    val showBackButton = mutableStateOf(false)
    val navigationBarTitle = mutableStateOf("")

    val notificationViewModel: NotificationViewModel
    val notificationFilterViewModel: NotificationFilterViewModel
    val manualTasks =
        ScheduleViewModel(CoreDashboardFilterViewModel(), MoreApplication.shared!!.dataRecorder, ScheduleListType.MANUALS)
    val runningSchedulesViewModel: ScheduleViewModel by lazy {
        ScheduleViewModel(
            CoreDashboardFilterViewModel(),
            MoreApplication.shared!!.dataRecorder,
            ScheduleListType.RUNNING
        )
    }
    val completedSchedulesViewModel: ScheduleViewModel by lazy {
        ScheduleViewModel(
            CoreDashboardFilterViewModel(),
            MoreApplication.shared!!.dataRecorder,
            ScheduleListType.COMPLETED
        )
    }
    val dashboardViewModel = DashboardViewModel(manualTasks)
    val settingsViewModel: SettingsViewModel by lazy { SettingsViewModel() }
    val studyDetailsViewModel: StudyDetailsViewModel by lazy { StudyDetailsViewModel() }
    val leaveStudyViewModel: LeaveStudyViewModel by lazy { LeaveStudyViewModel() }

    val taskCompletionBarViewModel = TaskCompletionBarViewModel()

    val infoVM: InfoViewModel by lazy {
        InfoViewModel()
    }

    private val simpleQuestionnaireViewModel by lazy {
        QuestionnaireViewModel()
    }

    private val simplePushButtonViewModel by lazy {
        PushButtonViewModel()
    }

    private val taskDetailsViewModel: TaskDetailsViewModel by lazy {
        TaskDetailsViewModel(MoreApplication.shared!!.dataRecorder)
    }

    init {
        val coreNotificationFilterViewModel = CoreNotificationFilterViewModel()
        notificationViewModel = NotificationViewModel(coreNotificationFilterViewModel)
        notificationFilterViewModel = NotificationFilterViewModel(coreNotificationFilterViewModel)
        MoreApplication.shared!!.showBleSetup().let { (firstTime, hasBLEObservations) ->
            if (hasBLEObservations) {
                if (firstTime) {
                    openBLESetupActivity(context)
                } else {
                    viewModelScope.launch(Dispatchers.IO) {
                        BluetoothDeviceRepository(MoreApplication.shared!!.mainBluetoothConnector).updateConnectedDevices()
                    }
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

    fun creteNewPushButtonViewModel(scheduleId: String): PushButtonViewModel {
        return simplePushButtonViewModel.apply { setScheduleId(scheduleId) }
    }

    fun createObservationDetailView(observationId: String): ObservationDetailsViewModel {
        return ObservationDetailsViewModel(observationId)
    }

    private fun openBLESetupActivity(context: Context) {
        (context as? Activity)?.let {
            val intent = Intent(context, BLEConnectionActivity::class.java)
            intent.putExtra(BLEConnectionActivity.SHOW_DESCR_PART2, true)
            it.startActivity(intent)
        }
    }
}