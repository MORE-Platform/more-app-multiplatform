package io.redlink.more.more_app_mutliplatform.android.activities.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.redlink.more.more_app_mutliplatform.android.activities.NavigationScreen
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardView
import io.redlink.more.more_app_mutliplatform.android.activities.info.InfoView
import io.redlink.more.more_app_mutliplatform.android.activities.setting.SettingsView
import io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.StudyDetailsView
import io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.StudyDetailsViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.tasks.TaskDetailsView
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground

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
    var title by remember { mutableStateOf("") }
    MoreBackground(
        navigationTitle = title,
        showBackButton = viewModel.showBackButton.value,
        onBackButtonClick = { navController.popBackStack() },
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
                title = NavigationScreen.DASHBOARD.stringRes()
                DashboardView(navController, viewModel = viewModel.dashboardViewModel)
            }
            composable(NavigationScreen.NOTIFICATIONS.route) {
                viewModel.tabIndex.value = 1
                viewModel.showBackButton.value = false
                title = NavigationScreen.NOTIFICATIONS.stringRes()
                Text("NotificationView")
            }
            composable(NavigationScreen.INFO.route) {
                viewModel.tabIndex.value = 2
                viewModel.showBackButton.value = false
                title = NavigationScreen.INFO.stringRes()
                InfoView(navController)
            }
            composable(NavigationScreen.SETTINGS.route) {
                title = NavigationScreen.SETTINGS.stringRes()
                viewModel.showBackButton.value = true
                SettingsView(model = viewModel.settingsViewModel)
            }
            composable(
                "${NavigationScreen.SCHEDULE_DETAILS.route}/observationId={observationId}&scheduleId={scheduleId}",
                arguments = listOf(
                    navArgument("observationId") {
                    type = NavType.StringType
                }, navArgument("scheduleId") {
                    type = NavType.StringType
                })
            ) {
                val arguments = requireNotNull(it.arguments)
                val observationId = arguments.getString("observationId")
                val scheduleId = arguments.getString("scheduleId")
                title = NavigationScreen.SCHEDULE_DETAILS.stringRes()
                viewModel.showBackButton.value = true
                TaskDetailsView(
                    viewModel = viewModel.taskDetailsViewModel,
                    scheduleViewModel = viewModel.dashboardViewModel.scheduleViewModel,
                    observationId = observationId,
                    scheduleId = scheduleId)
            }
            composable(NavigationScreen.STUDY_DETAILS.route) {
                title = NavigationScreen.STUDY_DETAILS.stringRes()
                viewModel.showBackButton.value = true
                StudyDetailsView(viewModel = StudyDetailsViewModel(viewModel.studyDetailsViewModel))
            }
        }
    }
}