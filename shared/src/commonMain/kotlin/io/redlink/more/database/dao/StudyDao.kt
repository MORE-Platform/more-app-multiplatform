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
package io.redlink.more.database.dao

import androidx.room.Dao
import androidx.room.Query
import io.redlink.more.database.entities.StudyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao : BaseDao<StudyEntity> {

    @Query("DELETE FROM studies WHERE studyId = :studyId")
    suspend fun deleteById(studyId: String)

    @Query("DELETE FROM studies")
    suspend fun deleteAll()

    @Query("SELECT * FROM studies WHERE studyId = :studyId")
    suspend fun getById(studyId: String): StudyEntity?

    @Query("SELECT * FROM studies WHERE studyId = :studyId")
    fun getByIdFlow(studyId: String): Flow<StudyEntity?>

    @Query("SELECT * FROM studies LIMIT 1")
    suspend fun get(): StudyEntity?

    @Query("SELECT * FROM studies LIMIT 1")
    fun getFlow(): Flow<StudyEntity?>

    @Query("SELECT * FROM studies WHERE active = :active")
    suspend fun getByActive(active: Boolean): List<StudyEntity>

    @Query("SELECT * FROM studies WHERE active = :active")
    fun getByActiveFlow(active: Boolean): Flow<List<StudyEntity>>

    @Query("SELECT * FROM studies WHERE state = :state")
    suspend fun getByState(state: String): List<StudyEntity>

    @Query("SELECT * FROM studies WHERE state = :state")
    fun getByStateFlow(state: String): Flow<List<StudyEntity>>

    @Query("SELECT * FROM studies WHERE participantId = :participantId")
    suspend fun getByParticipantId(participantId: Int): List<StudyEntity>

    @Query("SELECT * FROM studies WHERE start <= :timestamp AND `end` >= :timestamp")
    suspend fun getActiveStudiesAtTime(timestamp: Long): List<StudyEntity>

    @Query("SELECT COUNT(*) FROM studies")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM studies WHERE active = :active")
    suspend fun getCountByActive(active: Boolean): Int

    @Query("UPDATE studies SET state = :state WHERE studyId = :studyId")
    suspend fun updateStudyState(studyId: String, state: String)
}