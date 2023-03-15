package io.redlink.more.more_app_mutliplatform.android.activities

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource

enum class NavigationScreen(val route: String, @StringRes val stringResource: Int) {
    DASHBOARD("Dashboard", R.string.nav_dashboard),
    NOTIFICATIONS("Notifications", R.string.nav_notifications),
    INFO("Information", R.string.nav_info),
    SETTINGS("Settings", R.string.nav_settings),
    SCHEDULE_DETAILS("Task Details", R.string.nav_task_detail);

    @Composable
    fun stringRes() = getStringResource(id = stringResource)
}