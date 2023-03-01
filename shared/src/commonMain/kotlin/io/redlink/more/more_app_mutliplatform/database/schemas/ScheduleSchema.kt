package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationSchedule

class ScheduleSchema : RealmObject {
    var observationId: String = ""
    var start: RealmInstant? = null
    var end: RealmInstant? = null
    var done: Boolean = false

    companion object {
        fun toSchema(schedule: ObservationSchedule, id: String): ScheduleSchema {
            return ScheduleSchema().apply {
                observationId = id
                start = schedule.start?.toRealmInstant()
                end = schedule.end?.toRealmInstant()
            }
        }
    }
}