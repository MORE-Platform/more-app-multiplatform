package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import kotlinx.coroutines.flow.MutableStateFlow

class TaskDetailsModel(
    val observationTitle: String,
    val observationType: String,
    val observationId: String,
    val scheduleId: String,
    val start: Long?,
    val end: Long?,
    val participantInformation: String,
    val dataPointCount: MutableStateFlow<DataPointCountSchema?>
) {
    companion object {
        fun createModelsFrom(observation: ObservationSchema, schedule: ScheduleSchema, count: DataPointCountSchema?): TaskDetailsModel {
            return TaskDetailsModel(
                observationTitle = observation.observationTitle,
                observationType = observation.observationType,
                observationId = observation.observationId,
                scheduleId = schedule.scheduleId.toHexString(),
                start = schedule.start?.toInstant()?.epochSeconds,
                end = schedule.end?.toInstant()?.epochSeconds,
                participantInformation = observation.participantInfo,
                dataPointCount = MutableStateFlow(count)
            )
        }
    }
}