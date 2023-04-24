package io.redlink.more.app.android.activities.dashboard

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.extensions.formatDateFilterString
import io.redlink.more.app.android.extensions.getQuantityString
import io.redlink.more.app.android.extensions.getString
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(context: Context, coreFilterModel: CoreDashboardFilterViewModel,
                         val scheduleViewModel: ScheduleViewModel
): ViewModel() {
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

    init {
        scheduleViewModel.updateTaskStates(context)
        scope.launch {
            coreDashboardViewModel.study.collect {
                study.value = it
                study.value?.let { study ->
                    studyTitle.value = study.studyTitle
                }
            }
        }
    }

    fun getFilterString(): String {
        var filterString = ""
        val typesAmount = coreFilterViewModel.currentFilter.value.typeFilter.size
        val dateFilter = coreFilterViewModel.currentFilter.value.dateFilter.toString().formatDateFilterString()

        if(!coreFilterViewModel.hasDateFilter(DateFilterModel.ENTIRE_TIME))
            filterString += dateFilter

        if(!coreFilterViewModel.hasAllTypes()) {
            if(filterString.isNotBlank())
                filterString += ", "
            filterString += getQuantityString(R.plurals.filter_text, typesAmount, typesAmount)
        }

        if(filterString.isBlank())
            filterString += getString(R.string.more_filter_deactivated)

        return filterString
    }
}