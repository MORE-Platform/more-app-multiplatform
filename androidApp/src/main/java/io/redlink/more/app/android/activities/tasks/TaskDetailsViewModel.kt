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

import androidx.lifecycle.ViewModel
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.observations.DataRecorder
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.viewModels.tasks.CoreTaskDetailsViewModel

class TaskDetailsViewModel(
    dataRecorder: DataRecorder,
    observationFactory: ObservationFactory,
    scheduleId: String
) : ViewModel() {
    val coreViewModel: CoreTaskDetailsViewModel =
        CoreTaskDetailsViewModel(
            MoreApplication.shared!!.repositories,
            dataRecorder,
            scheduleId
        )

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