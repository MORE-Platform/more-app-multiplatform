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
    var dataPointCount: Long = 0
) {
    companion object {
        fun createModelFrom(observation: ObservationSchema, schedule: ScheduleSchema, count: Long): TaskDetailsModel {
            return TaskDetailsModel(
                observationTitle = observation.observationTitle,
                observationType = observation.observationType,
                observationId = observation.observationId,
                scheduleId = schedule.scheduleId.toHexString(),
                start = schedule.start?.toInstant()?.toEpochMilliseconds()?: 0,
                end = schedule.end?.toInstant()?.toEpochMilliseconds()?: 0,
                participantInformation = observation.participantInfo,
                dataPointCount = count,
                state = schedule.getState()
            )
        }
    }
}