/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.NavigationScreen.Companion.NavigationNotificationIDKey
import io.redlink.more.app.android.activities.completedSchedules.CompletedSchedulesView
import io.redlink.more.app.android.activities.dashboard.DashboardView
import io.redlink.more.app.android.activities.dashboard.filter.DashboardFilterView
import io.redlink.more.app.android.activities.dashboard.filter.DashboardFilterViewModel
import io.redlink.more.app.android.activities.info.InfoView
import io.redlink.more.app.android.activities.notification.NotificationView
import io.redlink.more.app.android.activities.notification.filter.NotificationFilterView
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireResponseView
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireView
import io.redlink.more.app.android.activities.runningSchedules.RunningSchedulesView
import io.redlink.more.app.android.activities.setting.SettingsView
import io.redlink.more.app.android.activities.setting.leave_study.LeaveStudyConfirmView
import io.redlink.more.app.android.activities.setting.leave_study.LeaveStudyView
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsView
import io.redlink.more.app.android.activities.studyDetails.observationDetails.ObservationDetailsView
import io.redlink.more.app.android.activities.studyStates.StudyClosedView
import io.redlink.more.app.android.activities.studyStates.StudyPausedView
import io.redlink.more.app.android.activities.studyStates.StudyUpdateView
import io.redlink.more.app.android.activities.tasks.TaskDetailsView
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.models.StudyState
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel

class MainActivity : ComponentActivity() {
    private var loadedNavController = false

    private lateinit var navHostController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = MainViewModel(this)

        val activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (::navHostController.isInitialized) {
                    navHostController.popBackStack()
                }
            }

        val destinationChangeListener =
            NavController.OnDestinationChangedListener { _, destination, _ ->
                viewModel.navigationBarTitle.value = destination.navigatorName
            }
        setContent {
            navHostController = rememberNavController()

            LaunchedEffect(Unit) {
                navHostController.addOnDestinationChangedListener(destinationChangeListener)
            }
            if (viewModel.studyIsUpdating.value) {
                StudyUpdateView()
                if (loadedNavController) {
                    navHostController.navigate(
                        NavigationScreen.DASHBOARD.navigationRoute()
                    )
                }
            } else if (viewModel.studyState.value == StudyState.PAUSED) {
                StudyPausedView()
            } else if (viewModel.studyState.value == StudyState.CLOSED) {
                StudyClosedView(viewModel.finishText.value)
            } else {
                MainView(
                    viewModel.navigationBarTitle.value,
                    viewModel,
                    navHostController,
                    activityLauncher
                )
                loadedNavController = true
            }
        }
    }
}

@Composable
fun MainView(
    navigationTitle: String,
    viewModel: MainViewModel,
    navController: NavHostController,
    activityResultLauncher: ActivityResultLauncher<Intent>
) {
    val currentContext = rememberUpdatedState(LocalContext.current)
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
                0 -> navController.navigate(NavigationScreen.DASHBOARD.routeWithParameters())
                1 -> navController.navigate(NavigationScreen.NOTIFICATIONS.routeWithParameters())
                2 -> navController.navigate(NavigationScreen.INFO.routeWithParameters())
            }
        },
        alertDialogModel = viewModel.alertDialogOpen.value
    ) {
        NavHost(
            navController = navController,
            startDestination = NavigationScreen.DASHBOARD.routeWithParameters()
        ) {
            NavigationScreen.DASHBOARD.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.tabIndex.value = 0
                    viewModel.showBackButton.value = false
                    viewModel.navigationBarTitle.value = screen.stringRes()
                    DashboardView(
                        navController, viewModel = viewModel.dashboardViewModel,
                        taskCompletionBarViewModel = viewModel.taskCompletionBarViewModel
                    )
                }
            }
            NavigationScreen.NOTIFICATIONS.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    viewModel.tabIndex.value = 1
                    viewModel.showBackButton.value = false
                    viewModel.navigationBarTitle.value = screen.stringRes()
                    NotificationView(navController, viewModel = viewModel.notificationViewModel)
                }
            }

            NavigationScreen.INFO.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    viewModel.tabIndex.value = 2
                    viewModel.showBackButton.value = false
                    viewModel.navigationBarTitle.value = screen.stringRes()
                    InfoView(navController, viewModel = viewModel.infoVM)
                }
            }

            NavigationScreen.SETTINGS.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    viewModel.navigationBarTitle.value = screen.stringRes()
                    viewModel.showBackButton.value = true
                    SettingsView(model = viewModel.settingsViewModel, navController = navController)
                }
            }
            NavigationScreen.SCHEDULE_DETAILS.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    val arguments = requireNotNull(it.arguments)
                    val scheduleId by remember {
                        mutableStateOf(requireNotNull(arguments.getString("scheduleId")))
                    }
                    val taskVM by remember { mutableStateOf(viewModel.getTaskDetailsVM(scheduleId)) }

                    viewModel.navigationBarTitle.value = screen.stringRes()
                    viewModel.showBackButton.value = true

                    TaskDetailsView(
                        navController = navController,
                        viewModel = taskVM,
                        scheduleId = scheduleId
                    )
                }
            }
            NavigationScreen.OBSERVATION_DETAILS.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    val arguments = requireNotNull(it.arguments)
                    viewModel.navigationBarTitle.value =
                        NavigationScreen.OBSERVATION_DETAILS.stringRes()
                    val observationId = arguments.getString("observationId")
                    viewModel.navigationBarTitle.value =
                        screen.stringRes()
                    viewModel.showBackButton.value = true

                    val obsDetailsVM by remember {
                        mutableStateOf(viewModel.createObservationDetailView(observationId ?: ""))
                    }

                    ObservationDetailsView(
                        viewModel = obsDetailsVM,
                        navController = navController
                    )
                }
            }

            NavigationScreen.STUDY_DETAILS.let { screen ->
                composable(
                    screen.routeWithParameters(), screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.navigationBarTitle.value = screen.stringRes()
                    viewModel.showBackButton.value = true

                    StudyDetailsView(
                        viewModel = viewModel.studyDetailsViewModel, navController = navController,
                        taskCompletionBarViewModel = viewModel.taskCompletionBarViewModel
                    )
                }
            }

            NavigationScreen.OBSERVATION_FILTER.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    viewModel.navigationBarTitle.value =
                        screen.stringRes()
                    viewModel.showBackButton.value = true

                    val arguments by remember { mutableStateOf(requireNotNull(it.arguments)) }
                    val vm by remember {
                        mutableStateOf(
                            when (ScheduleListType.valueOf(
                                arguments.getString(
                                    "scheduleListType",
                                    "ALL"
                                )
                            )) {
                                ScheduleListType.MANUALS -> viewModel.manualTasks.filterModel
                                ScheduleListType.RUNNING -> viewModel.runningSchedulesViewModel.filterModel
                                ScheduleListType.COMPLETED -> viewModel.completedSchedulesViewModel.filterModel
                                ScheduleListType.ALL -> DashboardFilterViewModel(
                                    CoreDashboardFilterViewModel()
                                )
                            }
                        )
                    }
                    DashboardFilterView(viewModel = vm)
                }
            }

            NavigationScreen.SIMPLE_QUESTION.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    val scheduleId by remember {
                        mutableStateOf(it.arguments?.getString("scheduleId"))
                    }
                    val observationId by remember {
                        mutableStateOf(it.arguments?.getString("observationId"))
                    }
                    val notificationId by remember {
                        mutableStateOf(it.arguments?.getString(NavigationNotificationIDKey))
                    }

                    viewModel.navigationBarTitle.value =
                        screen.stringRes()
                    viewModel.showBackButton.value = true
                    val vm by remember {
                        mutableStateOf(
                            viewModel.creteNewSimpleQuestionViewModel(
                                scheduleId,
                                observationId,
                                notificationId
                            )
                        )
                    }
                    QuestionnaireView(
                        navController = navController,
                        viewModel = vm
                    )
                }
            }

            NavigationScreen.LIMESURVEY.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    val scheduleId by remember {
                        mutableStateOf(it.arguments?.getString("scheduleId"))
                    }
                    val observationId by remember {
                        mutableStateOf(it.arguments?.getString("observationId"))
                    }
                    val notificationId by remember {
                        mutableStateOf(it.arguments?.getString(NavigationNotificationIDKey))
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (scheduleId != null || observationId != null) {
                            LaunchedEffect(Unit) {
                                viewModel.openLimesurvey(
                                    currentContext.value,
                                    activityResultLauncher,
                                    scheduleId,
                                    observationId,
                                    notificationId
                                )
                            }
                        }
                    }
                }
            }

            NavigationScreen.QUESTIONNAIRE_RESPONSE.let { screen ->
                composable(
                    screen.routeWithParameters(), screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.navigationBarTitle.value =
                        screen.stringRes()
                    viewModel.showBackButton.value = false

                    QuestionnaireResponseView(navController)
                }
            }

            NavigationScreen.NOTIFICATION_FILTER.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.navigationBarTitle.value =
                        screen.stringRes()
                    viewModel.showBackButton.value = true

                    NotificationFilterView(viewModel = viewModel.notificationFilterViewModel)
                }
            }

            NavigationScreen.RUNNING_SCHEDULES.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.navigationBarTitle.value =
                        screen.stringRes()
                    viewModel.showBackButton.value = true

                    RunningSchedulesView(
                        viewModel = viewModel.runningSchedulesViewModel,
                        navController = navController,
                        taskCompletionBarViewModel = viewModel.taskCompletionBarViewModel
                    )
                }
            }

            NavigationScreen.COMPLETED_SCHEDULES.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.navigationBarTitle.value =
                        screen.stringRes()
                    viewModel.showBackButton.value = true
                    CompletedSchedulesView(
                        viewModel = viewModel.completedSchedulesViewModel,
                        navController = navController,
                        taskCompletionBarViewModel = viewModel.taskCompletionBarViewModel
                    )
                }
            }

            NavigationScreen.LEAVE_STUDY.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.navigationBarTitle.value = screen.stringRes()
                    viewModel.showBackButton.value = true
                    LeaveStudyView(navController, viewModel = viewModel.leaveStudyViewModel)
                }
            }

            NavigationScreen.LEAVE_STUDY_CONFIRM.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.navigationBarTitle.value =
                        screen.stringRes()
                    viewModel.showBackButton.value = true
                    LeaveStudyConfirmView(navController, viewModel = viewModel.leaveStudyViewModel)
                }
            }
        }
    }
}