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

import io.redlink.more.more_app_mutliplatform.database.AppDatabase
import io.redlink.more.more_app_mutliplatform.database.entities.ObservationEntity
import io.redlink.more.more_app_mutliplatform.database.entities.ScheduleEntity
import io.redlink.more.more_app_mutliplatform.database.entities.StudyEntity
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

class StudyRepository(private val appDatabase: AppDatabase) {

    fun storeStudy(study: Study) {
        StudyScope.launch(Dispatchers.IO) {
            val studyEntity = StudyEntity.fromStudy(study)
            appDatabase.studyDao().insert(studyEntity)
        }

        StudyScope.launch(Dispatchers.IO) {
            val observationEntities = study.observations.map { ObservationEntity.toEntity(it) }
            appDatabase.observationDao().insertAll(observationEntities)
        }

        StudyScope.launch(Dispatchers.IO) {
            val scheduleEntities = study.observations.flatMap { observation ->
                observation.schedule.mapNotNull {
                    ScheduleEntity.fromObservationSchedule(
                        it,
                        observation.observationId,
                        observation.observationType,
                        observation.observationTitle,
                        observation.hidden ?: observation.noSchedule
                    )
                }
            }
            appDatabase.scheduleDao().insertAll(scheduleEntities)
        }
    }

    fun getStudy(): Flow<StudyEntity?> {
        return appDatabase.studyDao().getAllFlow().transform { emit(it.firstOrNull()) }
    }

    fun getStudyById(studyId: String): Flow<StudyEntity?> {
        return appDatabase.studyDao().getByIdFlow(studyId)
    }

    fun getStudyCount(): Flow<Int> {
        return appDatabase.studyDao().getCount()
    }
}