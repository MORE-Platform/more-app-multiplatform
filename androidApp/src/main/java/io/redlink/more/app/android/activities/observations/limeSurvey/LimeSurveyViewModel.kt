package io.redlink.more.app.android.activities.observations.limeSurvey

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.viewModels.limeSurvey.CoreLimeSurveyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LimeSurveyViewModel : ViewModel() {
    private val coreViewModel = CoreLimeSurveyViewModel(MoreApplication.observationManager!!)
    val limeSurveyLink = mutableStateOf<String?>(null)

    init {
        viewModelScope.launch {
            coreViewModel.limeSurveyLink.collect {
                withContext(Dispatchers.Main) {
                    limeSurveyLink.value = it
                }
            }
        }
    }

    fun setScheduleId(scheduleId: String) {
        coreViewModel.setScheduleId(scheduleId)
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
        limeSurveyLink.value = null
    }

    override fun onCleared() {
        super.onCleared()
        coreViewModel.close()
    }
}