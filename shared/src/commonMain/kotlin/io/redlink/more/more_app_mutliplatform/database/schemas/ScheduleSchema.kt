package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationSchedule
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import kotlinx.datetime.Clock
import org.mongodb.kbson.ObjectId

class ScheduleSchema : RealmObject {
    @PrimaryKey
    var scheduleId: ObjectId = ObjectId.invoke()
    var observationId: String = ""
    var observationType: String = ""
    var start: RealmInstant? = null
    var end: RealmInstant? = null
    var done: Boolean = false
    var state: String = ScheduleState.DEACTIVATED.name

    fun getState() = ScheduleState.getState(state)

    fun updateState(specificState: ScheduleState? = null): ScheduleState {
        specificState?.let {
            state = specificState.name
            return it
        }
        start?.let { start ->
            end?.let { end ->
                state = if (end.toInstant() <= Clock.System.now()) {
                    if (getState() == ScheduleState.RUNNING) {
                        ScheduleState.DONE.name
                    } else {
                        ScheduleState.ENDED.name
                    }
                } else if (start.toInstant() <= Clock.System.now() && getState() != ScheduleState.RUNNING) {
                    ScheduleState.ACTIVE.name
                } else if (getState() != ScheduleState.RUNNING) {
                    ScheduleState.DEACTIVATED.name
                } else {
                    ScheduleState.RUNNING.name
                }
            }
        }
        return getState()
    }

    companion object {
        fun toSchema(schedule: ObservationSchedule, observationId: String, observationType: String): ScheduleSchema? {
            return if (schedule.start != null && schedule.end != null){
                val scheduleState = if(schedule.start < Clock.System.now() && schedule.end > Clock.System.now()) {
                    ScheduleState.ACTIVE
                } else if (schedule.start > Clock.System.now()) {
                    ScheduleState.DEACTIVATED
                } else {
                    ScheduleState.ENDED
                }
                ScheduleSchema().apply {
                    this.observationId = observationId
                    this.observationType = observationType
                    start = schedule.start.toRealmInstant()
                    end = schedule.end.toRealmInstant()
                    state = scheduleState.name
                }
            } else null
        }
    }
}