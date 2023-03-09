package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.observations.Observation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transform

class ObservationRepository : Repository<ObservationSchema>() {

    override fun count(): Flow<Long> = realmDatabase.count<ObservationDataSchema>()

    fun observationWithUndoneSchedules(): Flow<List<ObservationSchema>> {
        return realmDatabase.query<ObservationSchema>(query = "schedules.done == false", limit = 1000)
    }

    suspend fun getObservationByObservationId(observationId: String): ObservationSchema? {
        return realmDatabase.queryFirst<ObservationSchema>(
            "observationId == $0",
            queryArgs = arrayOf(observationId)
        ).firstOrNull()
    }
}