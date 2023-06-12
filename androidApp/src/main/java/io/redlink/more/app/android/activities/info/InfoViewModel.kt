package io.redlink.more.app.android.activities.info

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoViewModel: ViewModel() {
    private val studyCoreViewModel = CoreStudyDetailsViewModel()
    val studyTitle = mutableStateOf("")
    val institute = studyCoreViewModel.studyModel.value?.study?.studyContact?.institute
    val contactPerson = studyCoreViewModel.studyModel.value?.study?.studyContact?.person
    val contactEmail = studyCoreViewModel.studyModel.value?.study?.studyContact?.email
    val contactPhoneNumber = studyCoreViewModel.studyModel.value?.study?.studyContact?.phoneNumber


    private var job: Job? = null

    fun viewDidAppear() {
       job = viewModelScope.launch {
            studyCoreViewModel.studyModel.collect{
                withContext(Dispatchers.Main) {
                    studyTitle.value = it?.study?.studyTitle ?: ""
                }
            }
        }
    }

    fun viewDidDisappear() {
        job?.cancel()
        studyCoreViewModel.viewDidDisappear()
    }
}