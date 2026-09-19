/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.app.android.activities.studyDetails.observationDetails

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.models.ObservationDetailsModel
import io.redlink.more.viewModels.observationDetails.CoreObservationDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObservationDetailsViewModel(
    observationId: String,
) : ViewModel() {

    val coreViewModel: CoreObservationDetailsViewModel =
        CoreObservationDetailsViewModel(
            MoreApplication.shared!!.repositories,
            observationId
        )
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
    }
}