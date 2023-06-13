package io.redlink.more.app.android.activities.info

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoViewModel: ViewModel() {
    private val coreViewModel = CoreStudyDetailsViewModel()
    private val studyRepository = StudyRepository()
    val model = mutableStateOf<StudyDetailsModel?>(null)
    val studyModel = mutableStateOf<StudySchema?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            studyRepository.getStudy().cancellable().collect {
                withContext(Dispatchers.Main) {
                    studyModel.value = it
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.studyModel.collect{
                withContext(Dispatchers.Main) {
                    model.value = it
                    println(it?.study)
                    println(it?.study?.studyTitle)
                    println(it?.study?.contact)
                    println("-----------------------")
                }
            }
        }
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }
}