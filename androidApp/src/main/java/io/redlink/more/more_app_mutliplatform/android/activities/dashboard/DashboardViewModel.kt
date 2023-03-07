package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.setting.SettingsActivity
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivity
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel: ViewModel() {
    private val coreDashboardViewModel: CoreDashboardViewModel = CoreDashboardViewModel()
    var study: MutableState<StudySchema?> = mutableStateOf(StudySchema())
    val studyTitle = mutableStateOf("Study Title")
    val studyActive = mutableStateOf(true)
    val currentTabIndex = MutableStateFlow(0)

    val totalTasks = mutableStateOf(0)
    val finishedTasks = mutableStateOf(0)
    val tabData = Views.values()

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    init {
        scope.launch {
            coreDashboardViewModel.loadStudy().collect {
                study.value = it
                study.value?.let { study ->
                    studyTitle.value = study.studyTitle
                }
            }
        }
    }

    fun openSettings(context: Context) {
        (context as? Activity)?.let {
            showNewActivity(it, SettingsActivity::class.java)
        }
    }
}