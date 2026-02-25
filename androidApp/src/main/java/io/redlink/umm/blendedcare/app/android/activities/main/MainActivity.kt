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
package io.redlink.umm.blendedcare.app.android.activities.main

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.redlink.umm.blendedcare.app.android.MoreApplication
import io.redlink.umm.blendedcare.app.android.activities.NavigationScreen
import io.redlink.umm.blendedcare.app.android.activities.NavigationScreen.Companion.NavigationNotificationIDKey
import io.redlink.umm.blendedcare.app.android.activities.completedSchedules.CompletedSchedulesView
import io.redlink.umm.blendedcare.app.android.activities.dashboard.DashboardView
import io.redlink.umm.blendedcare.app.android.activities.dashboard.filter.DashboardFilterView
import io.redlink.umm.blendedcare.app.android.activities.info.InfoView
import io.redlink.umm.blendedcare.app.android.activities.notification.NotificationView
import io.redlink.umm.blendedcare.app.android.activities.notification.filter.NotificationFilterView
import io.redlink.umm.blendedcare.app.android.activities.observationErrors.ObservationErrorView
import io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire.QuestionViewModel
import io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire.QuestionnaireResponseView
import io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire.QuestionnaireView
import io.redlink.umm.blendedcare.app.android.activities.runningSchedules.RunningSchedulesView
import io.redlink.umm.blendedcare.app.android.activities.setting.SettingsView
import io.redlink.umm.blendedcare.app.android.activities.setting.leave_study.LeaveStudyConfirmView
import io.redlink.umm.blendedcare.app.android.activities.setting.leave_study.LeaveStudyView
import io.redlink.umm.blendedcare.app.android.activities.studyDetails.StudyDetailsView
import io.redlink.umm.blendedcare.app.android.activities.studyDetails.observationDetails.ObservationDetailsView
import io.redlink.umm.blendedcare.app.android.activities.studyStates.StudyClosedView
import io.redlink.umm.blendedcare.app.android.activities.studyStates.StudyLoadingErrorView
import io.redlink.umm.blendedcare.app.android.activities.studyStates.StudyPausedView
import io.redlink.umm.blendedcare.app.android.activities.studyStates.StudyUpdateView
import io.redlink.umm.blendedcare.app.android.activities.taskCompletion.TaskCompletionBarViewModel
import io.redlink.umm.blendedcare.app.android.activities.tasks.TaskDetailsView
import io.redlink.umm.blendedcare.app.android.observations.PermissionUtils
import io.redlink.umm.blendedcare.app.android.shared_composables.MoreBackground
import io.redlink.umm.blendedcare.app.android.util.ActivityProvider
import io.redlink.umm.participant.models.ScheduleListType
import io.redlink.umm.participant.models.StudyState
import io.redlink.umm.participant.viewModels.ViewManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var navHostController: NavHostController

    override fun onResume() {
        super.onResume()
        ActivityProvider.setCurrentActivity(this)
    }

    override fun onPause() {
        super.onPause()
        ActivityProvider.clearCurrentActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        PermissionUtils.cleanupPermissionLauncher(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = MainViewModel(this)

        PermissionUtils.initializePermissionLauncher(this)

        val activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (::navHostController.isInitialized) {
                    navHostController.popBackStack()
                }
            }

        val destinationChangeListener =
            NavController.OnDestinationChangedListener { _, destination, _ ->
                val route = destination.route?.split("?")?.firstOrNull() ?: ""
                NavigationScreen.byRoute(route)?.let { screen ->
                    viewModel.navigationBarTitle.value = getString(screen.stringResource)
                }
            }

        setContent {
            navHostController = rememberNavController()

            val studyState by MoreApplication.shared!!.repositories.study.studyState.collectAsStateWithLifecycle()
            val studyIsUpdating by ViewManager.studyIsUpdating.collectAsStateWithLifecycle(false)
            val studyLoadingError by ViewManager.studyLoadingError.collectAsStateWithLifecycle(false)

            LaunchedEffect(Unit) {
                navHostController.addOnDestinationChangedListener(destinationChangeListener)
            }

            if (studyIsUpdating) {
                StudyUpdateView()
            } else if (studyLoadingError) {
                StudyLoadingErrorView()
            } else if (studyState == StudyState.PAUSED) {
                StudyPausedView()
            } else if (studyState == StudyState.CLOSED) {
                StudyClosedView()
            } else if (studyState == StudyState.NONE) {
                StudyUpdateView()
            } else {
                MainView(
                    viewModel.navigationBarTitle.value,
                    viewModel,
                    navHostController,
                    activityLauncher,
                    studyState
                )
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ViewManager.showGarminConnectView.collect {
                    if (it) {
                        while (!::navHostController.isInitialized) {
                            delay(500)
                        }
                        delay(500)
                        withContext(Dispatchers.Main) {
                            navHostController.navigate(NavigationScreen.GARMIN_CONNECT.routeWithParameters())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainView(
    navigationTitle: String,
    viewModel: MainViewModel,
    navController: NavHostController,
    activityResultLauncher: ActivityResultLauncher<Intent>,
    studyState: StudyState = StudyState.NONE,
) {
    val currentContext = rememberUpdatedState(LocalContext.current)
    val taskCompletionBarViewModel = remember { TaskCompletionBarViewModel() }
    val notificationCount =
        MoreApplication.shared!!.notificationManager.unreadUserCount.collectAsStateWithLifecycle()
    MoreBackground(
        navigationTitle = navigationTitle,
        showBackButton = viewModel.showBackButton.value,
        onBackButtonClick = { navController.navigateUp() },
        showTabRow = true,
        tabSelectionIndex = viewModel.tabIndex.intValue,
        onTabChange = {
            viewModel.showBackButton.value = false
            viewModel.tabIndex.intValue = it
            when (it) {
                0 -> navController.navigate(NavigationScreen.DASHBOARD.routeWithParameters())
                1 -> navController.navigate(NavigationScreen.NOTIFICATIONS.routeWithParameters())
                2 -> navController.navigate(NavigationScreen.INFO.routeWithParameters())
            }
        },
        unreadNotificationCount = notificationCount.value,
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
                    viewModel.tabIndex.intValue = 0
                    viewModel.showBackButton.value = false
                    DashboardView(
                        navController,
                        viewModel.manualTasks,
                        taskCompletionBarViewModel = taskCompletionBarViewModel
                    )
                }
            }
            NavigationScreen.NOTIFICATIONS.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    viewModel.tabIndex.intValue = 1
                    viewModel.showBackButton.value = false
                    NotificationView(navController, viewModel.coreNotificationFilterViewModel)
                }
            }

            NavigationScreen.INFO.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    viewModel.tabIndex.intValue = 2
                    viewModel.showBackButton.value = false
                    InfoView(navController)
                }
            }

            NavigationScreen.SETTINGS.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    viewModel.showBackButton.value = true
                    SettingsView()
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

                    viewModel.showBackButton.value = true

                    TaskDetailsView(
                        navController = navController,
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
                    val observationId = arguments.getString("observationId")
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
                    viewModel.showBackButton.value = true

                    StudyDetailsView(
                        navController = navController,
                        taskCompletionBarViewModel = taskCompletionBarViewModel
                    )
                }
            }

            NavigationScreen.OBSERVATION_FILTER.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()

                ) {
                    viewModel.showBackButton.value = true

                    val coreViewModel = remember {
                        viewModel.schedulesViewModel(
                            ScheduleListType.valueOf(
                                requireNotNull(it.arguments).getString(
                                    "scheduleListType",
                                    "ALL"
                                )
                            )
                        )
                    }.coreViewModel

                    DashboardFilterView(coreViewModel)
                }
            }

            NavigationScreen.QUESTION.let { screen ->
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

                    viewModel.showBackButton.value = true
                    val questionViewModel = remember(scheduleId, notificationId, observationId) {
                        QuestionViewModel(scheduleId, notificationId, observationId)
                    }
                    QuestionnaireView(
                        navController,
                        questionViewModel
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
                    viewModel.showBackButton.value = true

                    NotificationFilterView(coreViewModel = viewModel.coreNotificationFilterViewModel)
                }
            }

            NavigationScreen.RUNNING_SCHEDULES.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.showBackButton.value = true

                    RunningSchedulesView(
                        viewModel = viewModel.runningSchedulesViewModel,
                        navController = navController,
                        taskCompletionBarViewModel = taskCompletionBarViewModel
                    )
                }
            }

            NavigationScreen.COMPLETED_SCHEDULES.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.showBackButton.value = true
                    CompletedSchedulesView(
                        viewModel = viewModel.completedSchedulesViewModel,
                        navController = navController,
                        taskCompletionBarViewModel = taskCompletionBarViewModel
                    )
                }
            }

            NavigationScreen.GARMIN_CONNECT.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LaunchedEffect(Unit) {
                            viewModel.openGarminActivity(
                                currentContext.value,
                                activityResultLauncher
                            )
                        }
                    }
                }
            }

            NavigationScreen.LEAVE_STUDY.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.showBackButton.value = true
                    LeaveStudyView(navController)
                }
            }

            NavigationScreen.LEAVE_STUDY_CONFIRM.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.showBackButton.value = true
                    LeaveStudyConfirmView(navController)
                }
            }

            NavigationScreen.OBSERVATION_ERRORS.let { screen ->
                composable(
                    screen.routeWithParameters(),
                    screen.createListOfNavArguments(),
                    screen.createDeepLinkRoute()
                ) {
                    viewModel.showBackButton.value = true
                    ObservationErrorView()
                }
            }
        }
        LaunchedEffect(studyState) {
            if (studyState == StudyState.ACTIVE) {
                val currentBase = navController.currentDestination
                    ?.route
                    ?.substringBefore("?")

                val dashboardBase = NavigationScreen.DASHBOARD.routeWithParameters()

                if (currentBase != dashboardBase) {
                    navController.navigate(NavigationScreen.DASHBOARD.routeWithParameters()) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }
}
