package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DashboardViewModel: ViewModel() {
    private val coreDashboardViewModel: CoreDashboardViewModel = CoreDashboardViewModel()
    var study: MutableState<StudySchema?> = mutableStateOf(StudySchema())
    var studyTitle = mutableStateOf("")
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
}