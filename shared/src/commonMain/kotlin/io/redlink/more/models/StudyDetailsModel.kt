/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.models

import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.StudyEntity

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