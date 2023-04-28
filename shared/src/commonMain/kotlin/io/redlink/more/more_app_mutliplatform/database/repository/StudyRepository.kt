package io.redlink.more.more_app_mutliplatform.database.repository

import io.realm.kotlin.types.RealmObject
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import kotlinx.coroutines.flow.*

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
                    observation.observationType
                )
            }
        }.flatten())
        realmDatabase.store(realmObjects)
    }

    fun getStudy(): Flow<StudySchema?> {
        return realmDatabase.queryFirst()
    }

    override fun count(): Flow<Long> = realmDatabase.count<StudySchema>()
}