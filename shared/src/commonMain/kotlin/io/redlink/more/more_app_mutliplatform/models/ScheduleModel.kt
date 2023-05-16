package io.redlink.more.more_app_mutliplatform.models

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

    fun isSameAs(other: ScheduleModel) = this.scheduleId == other.scheduleId

    fun hasSameContentAs(other: ScheduleModel): Boolean {
        return this.done == other.done
                && this.start == other.start
                && this.end == other.end
                && this.scheduleState == other.scheduleState
    }


    companion object {

        fun createModel(schedule: ScheduleSchema): ScheduleModel? {
            val start = schedule.start ?: return null
            val end = schedule.end ?: return null
            return ScheduleModel(
                scheduleId = schedule.scheduleId.toHexString(),
                observationId = schedule.observationId,
                observationType = schedule.observationType,
                observationTitle = schedule.observationTitle,
                done = schedule.done,
                start = start.epochSeconds,
                end = end.epochSeconds,
                scheduleState = schedule.getState()
            )
        }
    }
}
