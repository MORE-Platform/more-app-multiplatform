package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant

data class StudyDetailsModel(
    val studyTitle: String,
    val participantInformation: String,
    val consentInfo: String,
    val start: Long?,
    val end: Long?,
    val observations: List<ObservationSchema>,
    val version: Long,
    var active: Boolean?,
    var totalTasks: Long,
    var finishedTasks: Long
) {
    companion object {
        fun createModelFrom(study: StudySchema, totalTasks: Long, finishedTasks: Long
        ): StudyDetailsModel {
            return StudyDetailsModel(
                studyTitle = study.studyTitle,
                participantInformation = study.participantInfo,
                consentInfo = study.consentInfo,
                start = study.start?.toInstant()?.toEpochMilliseconds(),
                end = study.end?.toInstant()?.toEpochMilliseconds(),
                observations = study.observations,
                version = study.version,
                active = study.active,
                totalTasks = totalTasks,
                finishedTasks = finishedTasks
            )
        }
    }
}