package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import kotlinx.datetime.Instant

data class ScheduleModel(
    val scheduleId: String,
    val observationId: String,
    val observationType: String,
    val observationTitle: String,
    val done: Boolean,
    val start: Long,
    val end: Long,
    var currentlyRunning: Boolean = false
){
    companion object {
        fun createModelsFrom(observation: ObservationSchema): List<ScheduleModel> {
            return observation.schedules.mapNotNull {
                val start = it.start ?: return@mapNotNull null
                val end = it.end ?: return@mapNotNull null
                ScheduleModel(
                    scheduleId = it.scheduleId.toHexString(),
                    observationId = observation.observationId,
                    observationType = observation.observationType,
                    observationTitle = observation.observationTitle,
                    done = it.done,
                    start = start.toInstant().toEpochMilliseconds(),
                    end = end.toInstant().toEpochMilliseconds()
                )
            }
        }
    }
}
