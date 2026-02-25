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
package io.redlink.umm.participant.models

import io.redlink.umm.participant.database.entities.ObservationEntity
import io.redlink.umm.participant.database.entities.StudyEntity

data class StudyDetailsModel(
    val study: StudyEntity,
    val observations: List<ObservationEntity>,
    var totalTasks: Long,
    var finishedTasks: Long
) {
    companion object {
        fun createModelFrom(
            study: StudyEntity,
            observations: List<ObservationEntity>,
            totalTasks: Long,
            finishedTasks: Long
        ): StudyDetailsModel {
            return StudyDetailsModel(
                study = study,
                observations = observations,
                totalTasks = totalTasks,
                finishedTasks = finishedTasks
            )
        }
    }
}