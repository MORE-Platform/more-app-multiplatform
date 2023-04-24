package io.redlink.more.app.android.activities.main

import ObservationDetailsView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.bluetooth_conntection_view.BluetoothConnectionView
import io.redlink.more.app.android.activities.bluetooth_conntection_view.BluetoothConnectionViewModel
import io.redlink.more.app.android.activities.dashboard.DashboardView
import io.redlink.more.app.android.activities.dashboard.filter.DashboardFilterView
import io.redlink.more.app.android.activities.dashboard.filter.DashboardFilterViewModel
import io.redlink.more.app.android.activities.info.InfoView
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireResponseView
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireView
import io.redlink.more.app.android.activities.setting.SettingsView
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsView
import io.redlink.more.app.android.activities.tasks.ObservationDetailsViewModel
import io.redlink.more.app.android.activities.tasks.TaskDetailsView
import io.redlink.more.app.android.shared_composables.MoreBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = MainViewModel(this)
        val destinationChangeListener = NavController.OnDestinationChangedListener { controller, destination, arguments ->
            viewModel.navigationBarTitle
        }
        setContent {
            val navController = rememberNavController()
            LaunchedEffect(Unit) {
                navController.addOnDestinationChangedListener(destinationChangeListener)
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
                DashboardView(navController, viewModel = viewModel.dashboardViewModel)
            }
            composable(NavigationScreen.NOTIFICATIONS.route) {
                viewModel.tabIndex.value = 1
                viewModel.showBackButton.value = false
                viewModel.navigationBarTitle.value = NavigationScreen.NOTIFICATIONS.stringRes()
                Text("NotificationView")
            }
            composable(NavigationScreen.INFO.route) {
                viewModel.tabIndex.value = 2
                viewModel.showBackButton.value = false
                viewModel.navigationBarTitle.value = NavigationScreen.INFO.stringRes()
                InfoView(navController)
            }
            composable(NavigationScreen.SETTINGS.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.SETTINGS.stringRes()
                viewModel.showBackButton.value = true
                SettingsView(model = viewModel.settingsViewModel)
            }
            composable(
                "${NavigationScreen.SCHEDULE_DETAILS.route}/observationTitle={observationTitle}&scheduleId={scheduleId}",
                arguments = listOf(
                    navArgument("observationTitle") {
                    type = NavType.StringType
                }, navArgument("scheduleId") {
                    type = NavType.StringType
                })
            ) {
                val arguments = requireNotNull(it.arguments)
                val scheduleId = arguments.getString("scheduleId")
                viewModel.navigationBarTitle.value = NavigationScreen.SCHEDULE_DETAILS.stringRes()
                val observationTitle = arguments.getString("observationTitle")
                viewModel.navigationBarTitle.value = NavigationScreen.SCHEDULE_DETAILS.stringRes()
                viewModel.showBackButton.value = true
                TaskDetailsView(
                    navController = navController,
                    viewModel = viewModel.createNewTaskViewModel(scheduleId ?: ""),
                    scheduleId = scheduleId,
                    observationTitle = observationTitle ?: ""
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
                )
            }

            composable(NavigationScreen.STUDY_DETAILS.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.STUDY_DETAILS.stringRes()
                viewModel.showBackButton.value = true
                StudyDetailsView(viewModel = viewModel.studyDetailsViewModel, navController = navController)
            }
            composable(NavigationScreen.OBSERVATION_FILTER.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.OBSERVATION_FILTER.stringRes()
                viewModel.showBackButton.value = true
                DashboardFilterView(viewModel = DashboardFilterViewModel(viewModel.dashboardFilterViewModel))
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
                QuestionnaireView(navController = navController, model = viewModel.creteNewSimpleQuestionViewModel(scheduleId ?: "", LocalContext.current))
            }
            composable(NavigationScreen.QUESTIONNAIRE_RESPONSE.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.QUESTIONNAIRE_RESPONSE.stringRes()
                viewModel.showBackButton.value = false
                QuestionnaireResponseView(navController)
            }

            composable(NavigationScreen.BLUETOOTH_CONNECTION.route) {
                viewModel.navigationBarTitle.value = NavigationScreen.BLUETOOTH_CONNECTION.stringRes()
                viewModel.showBackButton.value = true
                BluetoothConnectionView(navController, viewModel.bluetoothConnectionViewModel)
            }
        }
    }
}