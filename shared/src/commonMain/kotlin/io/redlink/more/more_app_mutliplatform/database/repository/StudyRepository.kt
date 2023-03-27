package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyRepository: Repository<StudySchema>() {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    suspend fun storeStudy(study: Study) {
        val studyItem = StudySchema.toSchema(study)
        setCache(studyItem)
        realmDatabase.storeAsync(studyItem)
    }

    fun getStudy(): Flow<StudySchema?> {
        val cache = readCache()
        if (cache != null) {
            return flowOf(cache)
        }
        scope.launch {
            setCache(realmDatabase.queryFirst<StudySchema>().firstOrNull())
        }
        return realmDatabase.queryFirst()
    }

    override fun count(): Flow<Long> = realmDatabase.count<StudySchema>()
}