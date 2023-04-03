package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getString
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidDataRecorder
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(context: Context, coreFilterModel: CoreDashboardFilterViewModel): ViewModel() {
    private val coreDashboardViewModel: CoreDashboardViewModel = CoreDashboardViewModel()
    private val coreFilterViewModel = coreFilterModel
    var study: MutableState<StudySchema?> = mutableStateOf(StudySchema())
    val studyTitle = mutableStateOf("Study Title")
    val studyActive = mutableStateOf(true)
    val currentTabIndex = MutableStateFlow(0)

    val totalTasks = mutableStateOf(0)
    val finishedTasks = mutableStateOf(0)
    val tabData = Views.values()

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    val scheduleViewModel = ScheduleViewModel(AndroidDataRecorder(context), coreFilterViewModel)

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

    fun getFilterString(): String {
        if(!coreFilterViewModel.hasAllTypes() && !coreFilterViewModel.hasDateFilter(DateFilterModel.ENTIRE_TIME))
            return getString(R.string.more_filter_date_type)
        if(!coreFilterViewModel.hasAllTypes())
            return getString(R.string.more_filter_type)
        if(!coreFilterViewModel.hasDateFilter(DateFilterModel.ENTIRE_TIME))
            return getString(R.string.more_filter_date)
        return getString(R.string.more_filter_deactivated)
    }
}