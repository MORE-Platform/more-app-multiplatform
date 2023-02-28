package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.database.converters.SchemaConverter
import io.redlink.more.more_app_mutliplatform.database.schemas.Observation
import io.redlink.more.more_app_mutliplatform.database.schemas.Schedule
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study

class StudyRepository {

    fun storeStudy(study: Study) {
        RealmDatabase.open(setOf(io.redlink.more.more_app_mutliplatform.database.schemas.Study::class, Observation::class, Schedule::class))
        RealmDatabase.store(SchemaConverter.toSchema(study))
        RealmDatabase.close()
    }
}