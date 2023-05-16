package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant

data class TaskDetailsModel(
    val observationTitle: String,
    val observationType: String,
    val observationId: String,
    val scheduleId: String,
    val start: Long,
    val end: Long,
    val participantInformation: String,
    val state: ScheduleState,
) {
    companion object {
        fun createModelFrom(observation: ObservationSchema, schedule: ScheduleSchema): TaskDetailsModel {
            return TaskDetailsModel(
                observationTitle = observation.observationTitle,
                observationType = observation.observationType,
                observationId = observation.observationId,
                scheduleId = schedule.scheduleId.toHexString(),
                start = schedule.start?.epochSeconds ?: 0,
                end = schedule.end?.epochSeconds ?: 0,
                participantInformation = observation.participantInfo,
                state = schedule.getState()
            )
        }
    }
}