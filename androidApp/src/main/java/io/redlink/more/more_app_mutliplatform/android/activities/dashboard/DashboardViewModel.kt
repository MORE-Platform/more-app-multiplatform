package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidDataRecorder
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(context: Context): ViewModel() {
    private val coreDashboardViewModel: CoreDashboardViewModel = CoreDashboardViewModel()
    var study: MutableState<StudySchema?> = mutableStateOf(StudySchema())
    val studyTitle = mutableStateOf("Study Title")
    val studyActive = mutableStateOf(true)
    val currentTabIndex = MutableStateFlow(0)

    val totalTasks = mutableStateOf(0)
    val finishedTasks = mutableStateOf(0)
    val tabData = Views.values()

    private val observationFactory = AndroidObservationFactory(context)

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    val scheduleViewModel = ScheduleViewModel(observationFactory, AndroidDataRecorder(context))

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
}