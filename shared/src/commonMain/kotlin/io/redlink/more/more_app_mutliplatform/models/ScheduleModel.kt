package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant

data class ScheduleModel(
    val scheduleId: String,
    val observationId: String,
    val observationType: String,
    val observationTitle: String,
    val done: Boolean,
    val start: Long,
    val end: Long,
    var scheduleState: ScheduleState = ScheduleState.DEACTIVATED
) {
    companion object {
        fun createModelsFrom(observationTitle: String, schedules: List<ScheduleSchema>): List<ScheduleModel> {
            return schedules.mapNotNull {
                val start = it.start ?: return@mapNotNull null
                val end = it.end ?: return@mapNotNull null
                ScheduleModel(
                    scheduleId = it.scheduleId.toHexString(),
                    observationId = it.observationId,
                    observationType = it.observationType,
                    observationTitle = observationTitle,
                    done = it.done,
                    start = start.toInstant().toEpochMilliseconds(),
                    end = end.toInstant().toEpochMilliseconds(),
                    scheduleState = if (it.done) ScheduleState.DEACTIVATED else it.getState()
                )
            }
        }
    }
}
