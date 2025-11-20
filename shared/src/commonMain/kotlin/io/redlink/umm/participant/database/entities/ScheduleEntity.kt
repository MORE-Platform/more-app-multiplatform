package io.redlink.umm.participant.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.redlink.umm.participant.models.ScheduleState
import io.redlink.umm.participant.services.network.openapi.model.ObservationSchedule
import io.redlink.umm.participant.util.createUUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey
    val scheduleId: String = createUUID(),
    val observationId: String = "",
    val observationType: String = "",
    val observationTitle: String = "",
    val start: Long? = null,
    val end: Long? = null,
    val done: Boolean = false,
    val hidden: Boolean = false,
    val state: String = ScheduleState.DEACTIVATED.name
) {
    fun getState() = ScheduleState.getState(state)
    fun startInstant() = start?.let { Instant.fromEpochSeconds(it) }
    fun endInstant() = end?.let { Instant.fromEpochSeconds(it) }

    fun updateState(specificState: ScheduleState? = null): ScheduleState {
        return if (specificState != null) {
            specificState
        } else {
            val now = Clock.System.now().epochSeconds
            start?.let { startTime ->
                end?.let { endTime ->
                    when {
                        endTime <= now -> {
                            if (getState().running()) {
                                ScheduleState.DONE
                            } else {
                                ScheduleState.ENDED
                            }
                        }

                        now < startTime -> ScheduleState.DEACTIVATED
                        startTime <= now && !getState().active() -> ScheduleState.ACTIVE
                        else -> ScheduleState.getState(state)
                    }
                }
            } ?: ScheduleState.getState(state)
        }
    }

    override fun toString(): String {
        return """ScheduleEntity: {"scheduleId": "$scheduleId", "observationId": "$observationId","observationType": "$observationType","observationTitle": "$observationTitle","start": "$start","end": "$end","done": $done,"hidden": $hidden,"state": "$state"}"""
    }

    companion object {
        fun fromObservationSchedule(
            schedule: ObservationSchedule,
            observationId: String,
            observationType: String,
            observationTitle: String,
            hidden: Boolean,
        ): ScheduleEntity? {
            return if (schedule.start != null && schedule.end != null) {
                val now = Clock.System.now().epochSeconds
                val scheduleState = when {
                    schedule.start.epochSeconds < now && schedule.end.epochSeconds > now -> ScheduleState.ACTIVE
                    schedule.start.epochSeconds > now -> ScheduleState.DEACTIVATED
                    else -> ScheduleState.ENDED
                }

                ScheduleEntity(
                    observationId = observationId,
                    observationType = observationType,
                    observationTitle = observationTitle,
                    start = schedule.start.epochSeconds,
                    end = schedule.end.epochSeconds,
                    hidden = hidden,
                    state = scheduleState.name
                )
            } else null
        }
    }
}
