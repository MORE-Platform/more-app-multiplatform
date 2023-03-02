package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StudyRepository {

    fun storeStudy(study: Study) {
        RealmDatabase.open(setOf(StudySchema::class, ObservationSchema::class, ScheduleSchema::class))
        RealmDatabase.store(StudySchema.toSchema(study))
        RealmDatabase.close()
    }

    fun getStudy(): Flow<List<StudySchema>> {
        RealmDatabase.open(setOf(StudySchema::class, ObservationSchema::class, ScheduleSchema::class))
        return RealmDatabase.query()
    }
}