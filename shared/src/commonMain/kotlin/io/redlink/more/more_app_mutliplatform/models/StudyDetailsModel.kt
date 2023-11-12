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
package io.redlink.more.more_app_mutliplatform.models

import io.realm.kotlin.ext.copyFromRealm
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema

data class StudyDetailsModel(
    val study: StudySchema,
    val observations: List<ObservationSchema>,
    var totalTasks: Long,
    var finishedTasks: Long
) {
    companion object {
        fun createModelFrom(study: StudySchema, observations: List<ObservationSchema>, totalTasks: Long, finishedTasks: Long): StudyDetailsModel {
            return StudyDetailsModel(
                study = study.copyFromRealm(),
                observations = observations,
                totalTasks = totalTasks,
                finishedTasks = finishedTasks
            )
        }
    }
}