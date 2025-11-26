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
package io.redlink.umm.participant.database.repository

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.umm.participant.database.AppDatabase
import io.redlink.umm.participant.database.entities.ObservationEntity
import io.redlink.umm.participant.database.entities.ScheduleEntity
import io.redlink.umm.participant.database.entities.StudyEntity
import io.redlink.umm.participant.extensions.mapState
import io.redlink.umm.participant.models.StudyState
import io.redlink.umm.participant.scopes.Scope
import io.redlink.umm.participant.scopes.StudyScope
import io.redlink.umm.participant.services.network.openapi.model.Study
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.transform

class StudyRepository(private val appDatabase: AppDatabase) {
    private val _study = MutableStateFlow<StudyEntity?>(null)

    @NativeCoroutines
    val study: StateFlow<StudyEntity?> = _study

    @NativeCoroutines
    val studyState: StateFlow<StudyState> =
        study.mapState(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)) {
            it?.let { StudyState.getState(it.state) } ?: StudyState.NONE
        }
    private val _finishText = MutableStateFlow<String?>(null)

    @NativeCoroutines
    val finishText: StateFlow<String?> = _finishText

    init {
        Scope.launch(Dispatchers.IO) {
            getStudy().collect {
                _study.value = it
                it?.let {
                    _finishText.value = it.finishText
                }
            }
        }
    }

    suspend fun upsert(study: Study) {
        deleteStudy()
        StudyScope.launch(Dispatchers.IO) {
            val studyEntity = StudyEntity.fromStudy(study)
            appDatabase.studyDao().insert(studyEntity)
            _finishText.value = study.finishText
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

    suspend fun updateStudyState(state: StudyState) {
        study.value?.let {
            appDatabase.studyDao().updateStudyState(it.studyId, state.descr)
        }
    }

    suspend fun deleteStudy() {
        study.value?.let {
            appDatabase.studyDao().deleteAll()
            appDatabase.observationDao().deleteAll()
            appDatabase.observationDataDao().deleteAll()
            appDatabase.scheduleDao().deleteAll()
            appDatabase.dataPointDao().deleteAll()
        }
    }
}