package io.redlink.more.more_app_mutliplatform.observations

import io.redlink.more.more_app_mutliplatform.viewModels.schedules.ScheduleState

data class ObservationManagerModel(
    val scheduleId: String,
    val observationId: String,
    val observationType: String,
    val observationUUID: String,
    var state: ScheduleState = ScheduleState.NON
)
