package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import kotlinx.coroutines.flow.Flow

class StudyRepository: Repository<StudySchema>() {

    fun storeStudy(study: Study) {
        realmDatabase.store(StudySchema.toSchema(study))
    }

    fun getStudy(): Flow<StudySchema?> {
        return realmDatabase.queryFirst()
    }

    override fun count(): Flow<Long> = realmDatabase.count<StudySchema>()
}