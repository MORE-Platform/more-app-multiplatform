package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant

data class ObservationDetailsModel(
    val observationTitle: String,
    val observationType: String,
    val observationId: String,
    val start: Long,
    val end: Long,
    val participantInformation: String
) {
    companion object {
        fun createModelFrom(observation: ObservationSchema, start: ScheduleSchema?, stop: ScheduleSchema?): ObservationDetailsModel {
            return ObservationDetailsModel(
                observationTitle = observation.observationTitle,
                observationType = observation.observationType,
                observationId = observation.observationId,
                start = start?.start?.toInstant()?.toEpochMilliseconds()?: 0,
                end = stop?.end?.toInstant()?.toEpochMilliseconds()?: 0,
                participantInformation = observation.participantInfo,
            )
        }
    }
}