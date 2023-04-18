package io.redlink.more.more_app_mutliplatform.android.activities.studyDetails

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.*

class StudyDetailsViewModel: ViewModel() {
    private val coreViewModel = CoreStudyDetailsViewModel()
    val model = mutableStateOf<StudyDetailsModel?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.studyModel.collect{
                withContext(Dispatchers.Main) {
                    model.value = it
                }
            }
        }
    }
}