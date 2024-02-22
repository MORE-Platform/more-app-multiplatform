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
import io.ktor.utils.io.core.use
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine

class CoreStudyDetailsViewModel: CoreViewModel() {
    val studyModel = MutableStateFlow<StudyDetailsModel?>(null)

    fun onLoadStudyDetails(provideNewState: ((StudyDetailsModel?) -> Unit)): Closeable {
        return studyModel.asClosure(provideNewState)
    }

    override fun viewDidAppear() {
        launchScope {
            StudyRepository().use { studyRepository ->
                ScheduleRepository().use { scheduleRepository ->
                    ObservationRepository().use { observationRepository ->
                        studyRepository.getStudy()
                            .combine(scheduleRepository.allSchedulesWithStatus(true).cancellable()) { study, doneTasks ->
                                Pair(study, doneTasks.size)
                            }.combine(scheduleRepository.count().cancellable()) { (studySchema, doneTaskCount), taskCount ->
                                Triple(
                                    studySchema,
                                    doneTaskCount,
                                    taskCount
                                )
                            }.combine(observationRepository.observations().cancellable()) { triple, observations ->
                                Pair(
                                    triple,
                                    observations,
                                )
                            }.cancellable().collect { (triple, observations) ->
                                triple.first?.let {studySchema ->
                                    println(studySchema)
                                    studyModel.value = StudyDetailsModel.createModelFrom(studySchema, observations, triple.third,
                                        triple.second.toLong()
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}