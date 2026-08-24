/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.database.repository

import io.redlink.more.database.AppDatabase
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.entities.StudyEntity
import io.redlink.more.extensions.mapState
import io.redlink.more.models.StudyState
import io.redlink.more.scopes.Scope
import io.redlink.more.services.network.openapi.model.Study
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

class StudyRepositoryImpl(private val appDatabase: AppDatabase) : StudyRepository {
    private val _study = MutableStateFlow<StudyEntity?>(null)

    override val study: StateFlow<StudyEntity?> = _study

    override val studyState: StateFlow<StudyState> =
        study.mapState(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)) {
            it?.let { StudyState.getState(it.state) } ?: StudyState.NONE
        }
    private val _finishText = MutableStateFlow<String?>(null)

    override val finishText: StateFlow<String?> = _finishText

    init {
        Scope.launch {
            getStudy().collectLatest {
                withContext(Dispatchers.Main) {
                    _study.value = it
                    it?.let {
                        _finishText.value = it.finishText
                    }
                }
            }
        }
    }

    override suspend fun upsert(study: Study) {
        deleteStudy()
        Scope.launch(Dispatchers.IO) {
            val studyEntity = StudyEntity.fromStudy(study)
            appDatabase.studyDao().insert(studyEntity)
            _finishText.value = study.finishText
        }

        Scope.launch(Dispatchers.IO) {
            val observationEntities =
                study.observations.map { ObservationEntity.toEntity(it) }
            appDatabase.observationDao().insertAll(observationEntities)
        }

        Scope.launch(Dispatchers.IO) {
            val scheduleEntities = study.observations.flatMap { observation ->
                observation.schedule.mapNotNull {
                    ScheduleEntity.fromObservationSchedule(
                        it,
                        observation.observationId,
                        observation.observationType,
                        observation.observationTitle,
                        observation.hidden ?: observation.noSchedule ?: false,
                        observation.reminder ?: false
                    )
                }
            }
            appDatabase.scheduleDao().insertAll(scheduleEntities)
        }
    }

    override fun getStudy(): Flow<StudyEntity?> {
        return appDatabase.studyDao().getFlow()
    }

    override suspend fun updateStudyState(state: StudyState) {
        study.value?.let {
            appDatabase.studyDao().updateStudyState(it.studyId, state.descr)
        }
    }

    override suspend fun deleteStudy() {
        appDatabase.studyDao().deleteAll()
        appDatabase.observationDao().deleteAll()
        appDatabase.observationDataDao().deleteAll()
        appDatabase.scheduleDao().deleteAll()
        appDatabase.dataPointDao().deleteAll()
    }
}
