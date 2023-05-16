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

    // TODO: inforopository with infodata (not yet in bakcend) - exchange mock data to backend data after it exists
    val institute: String = "Ludwig Boltzmann Institute "
    val contactPerson: String = "Dr. Markus Mustermann"
    val contactEmail: String? = "markus.mustermann@bolzmann.at"
    val contactTel: String? = null

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