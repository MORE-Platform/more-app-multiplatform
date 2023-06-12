package io.redlink.more.app.android.activities.info

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoViewModel: ViewModel() {
    private val coreViewModel = CoreStudyDetailsViewModel()
    private val studyRepository: StudyRepository = StudyRepository()
    val studyTitle = mutableStateOf("")
    val institute = mutableStateOf("")
    val contactPerson = mutableStateOf("")
    val contactEmail = mutableStateOf("")
    val contactPhoneNumber = mutableStateOf("")

    private var job: Job? = null


    fun viewDidAppear() {
        job = viewModelScope.launch {
            studyRepository.getStudy().cancellable().collect{
                withContext(Dispatchers.Main) {
                    println("view did appear------------------")
                    println(it)
                    studyTitle.value = it?.studyTitle ?: ""
                    institute.value = it?.contact?.institute ?: ""
                    contactPerson.value = it?.contact?.person ?: ""
                    contactEmail.value = it?.contact?.email ?: ""
                    contactPhoneNumber.value = it?.contact?.phoneNumber ?: ""
                }
            }
        }
    }

    fun viewDidDisappear() {
        job?.cancel()
        coreViewModel.viewDidDisappear()
    }
}