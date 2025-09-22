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
package io.redlink.more.more_app_mutliplatform.viewModels.studydetails

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull

class CoreStudyDetailsViewModel(shared: Shared) : CoreViewModel() {
    private val _studyModel = MutableStateFlow<StudyDetailsModel?>(null)

    @NativeCoroutines
    val studyModel: StateFlow<StudyDetailsModel?> = _studyModel

    init {
        launchScope {
            shared.repositories.study.study.combine(
                shared.repositories.schedule.allSchedulesWithStatus(true)
            ) { study, schedules ->
                Pair(study, schedules)
            }
                .combine(shared.repositories.observation.observations()) { (study, schedules), observations ->
                    val taskCount: Int =
                        shared.repositories.schedule.count().cancellable().firstOrNull() ?: 0
                    study?.let {
                        StudyDetailsModel.createModelFrom(
                            it,
                            observations.sortedBy { obs -> obs.observationTitle },
                            taskCount.toLong(),
                            schedules.size.toLong()
                        )
                    }
                }.collect { studyDetailsModel ->
                    _studyModel.value = studyDetailsModel
                }
        }
    }
}