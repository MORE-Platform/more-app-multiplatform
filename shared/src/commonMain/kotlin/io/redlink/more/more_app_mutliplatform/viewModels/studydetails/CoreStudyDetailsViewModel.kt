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

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.database.AppDatabase
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull

class CoreStudyDetailsViewModel(appDatabase: AppDatabase) : CoreViewModel() {
    val studyModel = MutableStateFlow<StudyDetailsModel?>(null)
    val studyRepository = StudyRepository(appDatabase)
    val scheduleRepository = ScheduleRepository(appDatabase)
    val observationRepository = ObservationRepository(appDatabase)

    fun onLoadStudyDetails(provideNewState: ((StudyDetailsModel?) -> Unit)): Closeable {
        return studyModel.asClosure(provideNewState)
    }

    override fun viewDidAppear() {
        launchScope(Dispatchers.IO) {
            val taskCount: Int = scheduleRepository.count().cancellable().firstOrNull() ?: 0

            studyRepository.getStudy().cancellable()
                .combine(
                    scheduleRepository.allSchedulesWithStatus(true).cancellable()
                ) { study, doneTasks ->
                    Pair(study, doneTasks.size)
                }.combine(
                    observationRepository.observations().cancellable()
                ) { (study, doneTaskCount), observations ->
                    study?.let { studySchema ->
                        studyModel.value = StudyDetailsModel.createModelFrom(
                            studySchema,
                            observations.sortedBy { it.observationTitle },
                            taskCount.toLong(),
                            doneTaskCount.toLong()
                        )
                    }
                }.collect { }
        }
    }
}