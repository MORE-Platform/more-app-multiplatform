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
package io.redlink.more.database.repository

import io.ktor.utils.io.core.Closeable
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow

interface ObservationRepository {

    suspend fun getCount(): Int

    fun observations(): Flow<List<ObservationEntity>>

    fun observationWithUndoneSchedules(): Flow<Map<ObservationEntity, List<ScheduleEntity>>>

    suspend fun updateLastCollection(type: String, timestamp: Long)

    suspend fun updateLastCollection(types: Set<String>, timestamp: Long)

    fun collectionTimestamp(type: String): Flow<Long?>

    fun collectAllTimestamps(): Flow<Map<String, Long>>

    fun collectTimestampForObservationIds(observationIds: Set<String>): Flow<Long>

    fun collectTimestampOfType(type: String, newState: (Long?) -> Unit): Closeable

    fun collectAllTimestamps(newState: (Map<String, Long>) -> Unit): Closeable

    fun collectObservationsWithUndoneSchedules(newState: (Map<ObservationEntity, List<ScheduleEntity>>) -> Unit): Closeable

    fun observationTypes(): Flow<Set<String>>

    fun observationById(observationId: String): Flow<ObservationEntity?>

    suspend fun getObservationByObservationId(observationId: String): ObservationEntity?
}