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