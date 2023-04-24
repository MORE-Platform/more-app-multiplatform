package io.redlink.more.app.android.activities

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource

enum class NavigationScreen(val route: String, @StringRes val stringResource: Int) {
    DASHBOARD("Dashboard", R.string.nav_dashboard),
    NOTIFICATIONS("Notifications", R.string.nav_notifications),
    INFO("Information", R.string.nav_info),
    SETTINGS("Settings", R.string.nav_settings),
    SCHEDULE_DETAILS("Task Details", R.string.nav_task_detail),
    OBSERVATION_DETAILS("Observation Details", R.string.nav_observation_detail),
    STUDY_DETAILS("Study Details", R.string.nav_study_details),
    OBSERVATION_FILTER("Observation Filter", R.string.nav_observation_filter),
    SIMPLE_QUESTION("Simple Observation", R.string.nav_simple_question),
    QUESTIONNAIRE_RESPONSE("Questionnaire Response", R.string.nav_simple_question),
    RUNNING_SCHEDULES("Running Observation", R.string.nav_running_schedules),
    COMPLETED_SCHEDULES("Completed Observations", R.string.nav_completed_schedules);

    @Composable
    fun stringRes() = getStringResource(id = stringResource)
}