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
package io.redlink.more.app.android.activities.tasks

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskDetailsViewModel(
    dataRecorder: DataRecorder,
    observationFactory: ObservationFactory,
    scheduleId: String
) : ViewModel() {
    val coreViewModel: CoreTaskDetailsViewModel =
        CoreTaskDetailsViewModel(
            MoreApplication.shared!!.repositories,
            dataRecorder,
            observationFactory,
            scheduleId
        )

    val polarHrReady = mutableStateOf(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            PolarHeartRateObservation.hrReady.collect {
                withContext(Dispatchers.Main) {
                    polarHrReady.value = it
                }
            }
        }
    }

    fun startObservation() {
        coreViewModel.startObservation()
    }

    fun pauseObservation() {
        coreViewModel.pauseObservation()
    }

    fun stopObservation() {
        coreViewModel.stopObservation()
    }
}