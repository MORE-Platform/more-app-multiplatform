package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.database.converters.SchemaConverter
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study

class StudyRepository {

    fun storeStudy(study: Study) {
        RealmDatabase.open(setOf(StudySchema::class, ObservationSchema::class, ScheduleSchema::class))
        RealmDatabase.store(SchemaConverter.toSchema(study))
        RealmDatabase.close()
    }
}