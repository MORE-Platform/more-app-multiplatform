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
package io.redlink.more.more_app_mutliplatform.database.repository

import io.realm.kotlin.types.RealmObject
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import kotlinx.coroutines.flow.Flow

class StudyRepository : Repository<StudySchema>() {
    fun storeStudy(study: Study) {
        val realmObjects = mutableListOf<RealmObject>()
        realmObjects.add(StudySchema.toSchema(study))
        realmObjects.addAll(study.observations.map { ObservationSchema.toSchema(it) })
        realmObjects.addAll(study.observations.map { observation ->
            observation.schedule.mapNotNull {
                ScheduleSchema.toSchema(
                    it,
                    observation.observationId,
                    observation.observationType,
                    observation.observationTitle,
                    observation.hidden ?: observation.noSchedule
                )
            }
        }.flatten())
        realmDatabase().store(realmObjects)
    }

    fun getStudy(): Flow<StudySchema?> {
        return realmDatabase().queryFirst()
    }

    override fun count(): Flow<Long> = realmDatabase().count<StudySchema>()
}