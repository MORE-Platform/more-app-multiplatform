/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema

data class ScheduleModel(
    val scheduleId: String,
    val observationId: String,
    val observationType: String,
    val observationTitle: String,
    val done: Boolean,
    val start: Long,
    val end: Long,
    val hidden: Boolean,
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
                hidden = schedule.hidden,
                scheduleState = schedule.getState()
            )
        }
    }
}
