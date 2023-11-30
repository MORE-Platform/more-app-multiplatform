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

import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant

data class ObservationDetailsModel(
    val observationTitle: String,
    val observationType: String,
    val observationId: String,
    val start: Long,
    val end: Long,
    val participantInformation: String
) {
    companion object {
        fun createModelFrom(observation: ObservationSchema, start: ScheduleSchema?, stop: ScheduleSchema?): ObservationDetailsModel {
            return ObservationDetailsModel(
                observationTitle = observation.observationTitle,
                observationType = observation.observationType,
                observationId = observation.observationId,
                start = start?.start?.toInstant()?.epochSeconds?: 0,
                end = stop?.end?.toInstant()?.epochSeconds?: 0,
                participantInformation = observation.participantInfo,
            )
        }
    }
}