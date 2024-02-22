/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.activities.studyDetails.observationDetails

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.models.ObservationDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.observationDetails.CoreObservationDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObservationDetailsViewModel(
    observationId: String,
): ViewModel() {

    private val coreViewModel: CoreObservationDetailsViewModel = CoreObservationDetailsViewModel(observationId)
    val observationDetailsModel = mutableStateOf(
        ObservationDetailsModel(
            "", "", "", 0, 0, ""
        )
    )

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