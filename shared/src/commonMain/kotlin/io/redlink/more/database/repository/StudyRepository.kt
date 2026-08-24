/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.database.repository

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.database.entities.StudyEntity
import io.redlink.more.models.StudyState
import io.redlink.more.services.network.openapi.model.Study
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface StudyRepository {
    @NativeCoroutines
    val study: StateFlow<StudyEntity?>

    @NativeCoroutines
    val studyState: StateFlow<StudyState>

    @NativeCoroutines
    val finishText: StateFlow<String?>

    suspend fun upsert(study: Study)

    fun getStudy(): Flow<StudyEntity?>

    suspend fun updateStudyState(state: StudyState)

    suspend fun deleteStudy()
}