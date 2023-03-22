package io.redlink.more.more_app_mutliplatform.models


import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Observation
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study

data class StudyDetailsModel(
    val studyTitle: String,
    val participantInformation: String,
    val start: Int,
    val end: Int,
    val observations: List<Observation>
) {
    companion object {
        fun createModelFrom(study: Study
        ): StudyDetailsModel {
            return StudyDetailsModel(
                studyTitle = study.studyTitle,
                participantInformation = study.participantInfo,
                start = study.start.toEpochDays(),
                end = study.end.toEpochDays(),
                observations = study.observations

            )
        }
    }
}