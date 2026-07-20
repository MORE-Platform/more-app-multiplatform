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
package io.redlink.more.models

import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity

data class TaskDetailsModel(
    val observationTitle: String,
    val observationType: String,
    val observationId: String,
    val scheduleId: String,
    val start: Long,
    val end: Long,
    val participantInformation: String,
    val hidden: Boolean,
    val state: ScheduleState,
) {
    companion object {
        fun createModelFrom(
            observation: ObservationEntity,
            schedule: ScheduleEntity
        ): TaskDetailsModel {
            return TaskDetailsModel(
                observationTitle = observation.observationTitle,
                observationType = observation.observationType,
                observationId = observation.observationId,
                scheduleId = schedule.scheduleId,
                start = schedule.start ?: 0,
                end = schedule.end ?: 0,
                participantInformation = observation.participantInfo,
                hidden = schedule.hidden,
                state = schedule.getState()
            )
        }
    }
}