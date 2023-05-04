package io.redlink.more.app.android.activities.main

import io.redlink.more.app.android.activities.notification.filter.NotificationFilterView
import ObservationDetailsView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.bluetooth_conntection_view.BluetoothConnectionView
import io.redlink.more.app.android.activities.completedSchedules.CompletedSchedulesView
import io.redlink.more.app.android.activities.dashboard.DashboardView
import io.redlink.more.app.android.activities.dashboard.filter.DashboardFilterView
import io.redlink.more.app.android.activities.dashboard.filter.DashboardFilterViewModel
import io.redlink.more.app.android.activities.info.InfoView
import io.redlink.more.app.android.activities.notification.NotificationView
import io.redlink.more.app.android.activities.info.InfoViewModel
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireResponseView
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireView
import io.redlink.more.app.android.activities.runningSchedules.RunningSchedulesView
import io.redlink.more.app.android.activities.setting.SettingsView
import io.redlink.more.app.android.activities.setting.leave_study.LeaveStudyConfirmView
import io.redlink.more.app.android.activities.setting.leave_study.LeaveStudyView
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsView
import io.redlink.more.app.android.activities.tasks.TaskDetailsView
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = MainViewModel(this)
        val destinationChangeListener = NavController.OnDestinationChangedListener { controller, destination, arguments ->
            viewModel.navigationBarTitle.value = destination.navigatorName
        }
        setContent {
            val navController = rememberNavController()
            LaunchedEffect(Unit) {
                navController.addOnDestinationChangedListener(destinationChangeListener)
                viewModel.viewDidAppear()
            }
            MainView(viewModel.navigationBarTitle.value, viewModel, navController)
        }
    }
}

@Composable
fun MainView(navigationTitle: String, viewModel: MainViewModel, navController: NavHostController) {
    MoreBackground(
        navigationTitle = navigationTitle,
        showBackButton = viewModel.showBackButton.value,
        onBackButtonClick = { navController.navigateUp() },
        showTabRow = true,
        tabSelectionIndex = viewModel.tabIndex.value,
        onTabChange = {
            viewModel.showBackButton.value = false
            viewModel.tabIndex.value = it
            when (it) {
                0 -> navController.navigate(NavigationScreen.DASHBOARD.route)
                1 -> navController.navigate(NavigationScreen.NOTIFICATIONS.route)
                2 -> navController.navigate(NavigationScreen.INFO.route)
            }
        }) {
        NavHost(navController = navController, startDestination = NavigationScreen.DASHBOARD.route) {
            composable(NavigationScreen.DASHBOARD.route) {
                viewModel.tabIndex.value = 0
                viewModel.showBackButton.value = false
                viewModel.navigationBarTitle.value = NavigationScreen.DASHBOARD.stringRes()
                DashboardView(navController, viewModel = viewModel.dashboardViewModel,
                    taskCompletionBarViewModel = viewModel.taskCompletionBarViewModel)
            }
            composable(NavigationScreen.NOTIFICATIONS.route) {
                viewModel.tabIndex.value = 1
                viewModel.showBackButton.value = false
                viewModel.navigationBarTitle.value = NavigationScreen.NOTIFICATIONS.stringRes()
                NotificationView(navController, viewModel = viewModel.notificationViewModel)
            }
            composable(NavigationScreen.INFO.route) {
                viewModel.tabIndex.value = 2
                viewModel.showBackButton.value = false
                viewModel.navigationBarTitle.value = NavigationScreen.INFO.stringRes()
                InfoView(navController, viewModel = InfoViewModel())
            }
            composable(NavigationScreen.SETTINGS.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.SETTINGS.stringRes()
                viewModel.showBackButton.value = true
                SettingsView(model = viewModel.settingsViewModel)
            }
            composable(
                "${NavigationScreen.SCHEDULE_DETAILS.route}/scheduleId={scheduleId}&scheduleListType={scheduleListType}",
                arguments = listOf(
                    navArgument("scheduleId") {
                    type = NavType.StringType
                })
            ) {
                val arguments = requireNotNull(it.arguments)
                val scheduleId = arguments.getString("scheduleId")
                viewModel.navigationBarTitle.value = NavigationScreen.SCHEDULE_DETAILS.stringRes()
                val scheduleListType: ScheduleListType = ScheduleListType.valueOf(arguments.getString("scheduleListType", "ALL"))
                viewModel.navigationBarTitle.value = NavigationScreen.SCHEDULE_DETAILS.stringRes()
                viewModel.showBackButton.value = true
                TaskDetailsView(
                    navController = navController,
                    viewModel = viewModel.getTaskDetailsVM(scheduleId ?: ""),
                    scheduleId = scheduleId,
                    scheduleListType = scheduleListType
                )
            }

            composable(
                "${NavigationScreen.OBSERVATION_DETAILS.route}/observationId={observationId}",
                arguments = listOf(
                    navArgument("observationId") {
                        type = NavType.StringType
                    })
            ) {
                val arguments = requireNotNull(it.arguments)
                viewModel.navigationBarTitle.value = NavigationScreen.OBSERVATION_DETAILS.stringRes()
                val observationId = arguments.getString("observationId")
                viewModel.navigationBarTitle.value = NavigationScreen.OBSERVATION_DETAILS.stringRes()
                viewModel.showBackButton.value = true
                ObservationDetailsView(
                    viewModel = viewModel.createObservationDetailView(observationId ?: ""),
                    navController = navController
                )
            }

            composable(NavigationScreen.STUDY_DETAILS.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.STUDY_DETAILS.stringRes()
                viewModel.showBackButton.value = true
                StudyDetailsView(viewModel = viewModel.studyDetailsViewModel, navController = navController,
                    taskCompletionBarViewModel = viewModel.taskCompletionBarViewModel)
            }
            composable("${NavigationScreen.OBSERVATION_FILTER.route}/scheduleListType={scheduleListType}",
            arguments = listOf(
                navArgument("scheduleListType") {
                    type = NavType.StringType
                })
            ) {
                viewModel.navigationBarTitle.value = NavigationScreen.OBSERVATION_FILTER.stringRes()
                viewModel.showBackButton.value = true
                val arguments = requireNotNull(it.arguments)
                when (ScheduleListType.valueOf(arguments.getString("scheduleListType", "ALL"))) {
                    ScheduleListType.ALL -> {
                        DashboardFilterView(viewModel = DashboardFilterViewModel(viewModel.allSchedulesViewModel.coreFilterModel))
                    }
                    ScheduleListType.RUNNING -> {
                        DashboardFilterView(viewModel = DashboardFilterViewModel(viewModel.runningSchedulesViewModel.coreFilterModel))
                    }
                    ScheduleListType.COMPLETED -> {
                        DashboardFilterView(viewModel = DashboardFilterViewModel(viewModel.completedSchedulesViewModel.coreFilterModel))
                    }
                }
            }
            composable(
                "${NavigationScreen.SIMPLE_QUESTION.route}/scheduleId={scheduleId}",
                arguments = listOf(
                    navArgument("scheduleId") {
                        type = NavType.StringType
                    })
            ) {
                val arguments = requireNotNull(it.arguments)
                val scheduleId = arguments.getString("scheduleId")
                viewModel.navigationBarTitle.value = NavigationScreen.SIMPLE_QUESTION.stringRes()
                viewModel.showBackButton.value = true
                QuestionnaireView(navController = navController, model = viewModel.creteNewSimpleQuestionViewModel(
                    scheduleId ?: ""
                ))
            }
            composable(NavigationScreen.QUESTIONNAIRE_RESPONSE.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.QUESTIONNAIRE_RESPONSE.stringRes()
                viewModel.showBackButton.value = false
                QuestionnaireResponseView(navController)
            }
            composable(NavigationScreen.NOTIFICATION_FILTER.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.NOTIFICATION_FILTER.stringRes()
                viewModel.showBackButton.value = true
                NotificationFilterView(viewModel = viewModel.notificationViewModel.filterModel)
            }

            composable(NavigationScreen.BLUETOOTH_CONNECTION.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.BLUETOOTH_CONNECTION.stringRes()
                viewModel.showBackButton.value = true
                BluetoothConnectionView(navController, viewModel.bluetoothConnectionViewModel)
            }
            composable(NavigationScreen.RUNNING_SCHEDULES.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.RUNNING_SCHEDULES.stringRes()
                viewModel.showBackButton.value = true
                RunningSchedulesView(
                    viewModel = viewModel.runningSchedulesViewModel,
                    navController = navController,
                    taskCompletionBarViewModel = viewModel.taskCompletionBarViewModel)
            }
            composable(NavigationScreen.COMPLETED_SCHEDULES.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.COMPLETED_SCHEDULES.stringRes()
                viewModel.showBackButton.value = true
                CompletedSchedulesView(
                    viewModel = viewModel.completedSchedulesViewModel,
                    navController = navController,
                    taskCompletionBarViewModel = viewModel.taskCompletionBarViewModel)
            }
            composable(NavigationScreen.LEAVE_STUDY.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.LEAVE_STUDY.stringRes()
                viewModel.showBackButton.value = true
                LeaveStudyView(navController, viewModel = viewModel.leaveStudyViewModel)
            }
            composable(NavigationScreen.LEAVE_STUDY_CONFIRM.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.LEAVE_STUDY_CONFIRM.stringRes()
                viewModel.showBackButton.value = true
                LeaveStudyConfirmView(navController, viewModel = viewModel.leaveStudyViewModel)
            }
        }
    }
}