package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.setting.SettingsActivity
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivity

class DashboardViewModel: ViewModel() {


    val scheduleViewModel = ScheduleViewModel()
    fun openSettings(context: Context) {
        (context as? Activity)?.let {
            showNewActivity(it, SettingsActivity::class.java)
        }
    }
}