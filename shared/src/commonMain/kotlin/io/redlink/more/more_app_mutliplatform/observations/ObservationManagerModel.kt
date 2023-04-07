package io.redlink.more.more_app_mutliplatform.observations

import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema

data class ObservationManagerModel(
    val schedule: ScheduleSchema,
    val config: Map<String, Any>,
)
