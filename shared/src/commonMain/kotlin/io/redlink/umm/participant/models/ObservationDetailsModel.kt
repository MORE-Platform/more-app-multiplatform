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
package io.redlink.umm.participant.models

import io.redlink.umm.participant.database.entities.ObservationEntity
import io.redlink.umm.participant.database.entities.ScheduleEntity

data class ObservationDetailsModel(
    val observationTitle: String,
    val observationType: String,
    val observationId: String,
    val start: Long,
    val end: Long,
    val participantInformation: String
) {
    companion object {
        fun createModelFrom(
            observation: ObservationEntity,
            start: ScheduleEntity?,
            stop: ScheduleEntity?
        ): ObservationDetailsModel {
            return ObservationDetailsModel(
                observationTitle = observation.observationTitle,
                observationType = observation.observationType,
                observationId = observation.observationId,
                start = start?.start ?: 0,
                end = stop?.end ?: 0,
                participantInformation = observation.participantInfo,
            )
        }
    }
}