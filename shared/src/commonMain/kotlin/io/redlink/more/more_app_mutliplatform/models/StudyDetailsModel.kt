package io.redlink.more.more_app_mutliplatform.models

import io.realm.kotlin.ext.copyFromRealm
import io.realm.kotlin.internal.platform.freeze
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant

data class StudyDetailsModel(
    val study: StudySchema,
    val observations: List<ObservationSchema>,
    var totalTasks: Long,
    var finishedTasks: Long
) {
    companion object {
        fun createModelFrom(study: StudySchema, observations: List<ObservationSchema>, totalTasks: Long, finishedTasks: Long): StudyDetailsModel {
            println("study details model-------------")
            println(study)
            println(study.studyTitle)
            println(study.contact)
            println("-----------------")
            return StudyDetailsModel(
                study = study.copyFromRealm(),
                observations = observations,
                totalTasks = totalTasks,
                finishedTasks = finishedTasks
            )
        }
    }
}