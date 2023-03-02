package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ObservationRepository : Repository<ObservationSchema>() {

    override fun count(): Flow<Long> = realmDatabase.count<ObservationDataSchema>()

    suspend fun getObservationByObservationId(observationId: String): ObservationSchema? {
        return realmDatabase.queryFirst<ObservationSchema>(
            "observationId == $0",
            queryArgs = arrayOf(observationId)
        ).firstOrNull()
    }
}