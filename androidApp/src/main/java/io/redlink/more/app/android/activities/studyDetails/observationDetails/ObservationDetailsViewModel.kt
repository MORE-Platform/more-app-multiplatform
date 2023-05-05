package io.redlink.more.app.android.activities.tasks

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.models.ObservationDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.observationDetails.CoreObservationDetailsViewModel
import kotlinx.coroutines.*

class ObservationDetailsViewModel(
    observationId: String,
): ViewModel() {

    private val coreViewModel: CoreObservationDetailsViewModel = CoreObservationDetailsViewModel(observationId)
    val observationDetailsModel = mutableStateOf(
        ObservationDetailsModel(
            "", "", "", 0, 0, ""
        )
    )
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.observationDetailsModel.collect { details ->
                details?.let {
                    withContext(Dispatchers.Main) {
                        observationDetailsModel.value = it
                    }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                coreViewModel.viewDidAppear()
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