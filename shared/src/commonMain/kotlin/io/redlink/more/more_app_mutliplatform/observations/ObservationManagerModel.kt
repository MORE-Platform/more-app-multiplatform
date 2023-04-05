package io.redlink.more.more_app_mutliplatform.observations

import io.redlink.more.more_app_mutliplatform.models.ScheduleState

data class ObservationManagerModel(
    val scheduleId: String,
    val observationId: String,
    val observationType: String,
    val observationUUID: String,
    val config: Map<String, Any>,
    var state: ScheduleState = ScheduleState.DEACTIVATED
)
