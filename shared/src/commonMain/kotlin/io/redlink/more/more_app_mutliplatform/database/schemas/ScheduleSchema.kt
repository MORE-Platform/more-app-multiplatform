package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationSchedule
import kotlinx.datetime.Clock
import org.mongodb.kbson.ObjectId

class ScheduleSchema : RealmObject {
    @PrimaryKey
    var scheduleId: ObjectId = ObjectId.invoke()
    var observationId: String = ""
    var observationType: String = ""
    var observationTitle: String = ""
    var start: RealmInstant? = null
    var end: RealmInstant? = null
    var done: Boolean = false
    var state: String = ScheduleState.DEACTIVATED.name

    fun getState() = ScheduleState.getState(state)

    fun updateState(specificState: ScheduleState? = null): ScheduleState {
        if (specificState != null) {
            state = specificState.name
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            (start?.epochSeconds?.times(1000))?.let { start ->
                (end?.epochSeconds?.times(1000))?.let { end ->
                    state = if (end <= now) {
                        if (getState() == ScheduleState.RUNNING) {
                            ScheduleState.DONE.name
                        } else {
                            ScheduleState.ENDED.name
                        }
                    } else if (start <= now && getState() != ScheduleState.RUNNING) {
                        ScheduleState.ACTIVE.name
                    } else if (getState() != ScheduleState.RUNNING) {
                        ScheduleState.DEACTIVATED.name
                    } else {
                        ScheduleState.RUNNING.name
                    }
                }
            }
        }
        if (getState() == ScheduleState.DONE) {
            done = true
        }
        return getState()
    }

    fun equalsSchedule(other: ScheduleSchema): Boolean {
        return scheduleId == other.scheduleId
    }

    companion object {
        fun toSchema(
            schedule: ObservationSchedule,
            observationId: String,
            observationType: String,
            observationTitle: String
        ): ScheduleSchema? {
            return if (schedule.start != null && schedule.end != null) {
                val now = Clock.System.now().epochSeconds
                val scheduleState =
                    if (schedule.start.epochSeconds < now && schedule.end.epochSeconds > now) {
                        ScheduleState.ACTIVE
                    } else if (schedule.start.epochSeconds > now) {
                        ScheduleState.DEACTIVATED
                    } else {
                        ScheduleState.ENDED
                    }
                ScheduleSchema().apply {
                    this.observationId = observationId
                    this.observationType = observationType
                    this.observationTitle = observationTitle
                    start = schedule.start.toRealmInstant()
                    end = schedule.end.toRealmInstant()
                    state = scheduleState.name
                }
            } else null
        }
    }
}