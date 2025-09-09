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
import io.redlink.more.more_app_mutliplatform.database.entities.DataPointEntity
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DataPointCountRepository(private val appDatabase: AppDatabase) {
    private val mutex = Mutex()
    private val countQueue = mutableMapOf<String, Long>()
    private var storeJob: Job? = null

    fun count(): Flow<Long> {
        return appDatabase.dataPointDao().getCountFlow()
    }

    fun incrementCount(scheduleIdSet: Set<String>, addCount: Long = 1) {
        if (scheduleIdSet.isNotEmpty()) {
            StudyScope.launch(Dispatchers.IO) {
                mutex.withLock {
                    scheduleIdSet.forEach { scheduleId ->
                        countQueue[scheduleId] = countQueue.getOrElse(scheduleId) { 0 } + addCount
                    }
                }
                if (storeJob == null || storeJob?.isActive == false) {
                    storeJob = StudyScope.repeatedLaunch(5000L, Dispatchers.IO) {
                        storeCounts()
                    }.second
                    storeJob?.invokeOnCompletion {
                        storeJob = null
                    }
                }
            }
        }
    }

    private suspend fun storeCounts() {
        val countsToStore: Map<String, Long>
        mutex.withLock {
            countsToStore = countQueue.toMap()
            countQueue.clear()
        }
        if (countsToStore.isNotEmpty()) {
            StudyScope.launch(Dispatchers.IO) {
                val allDataPoints = appDatabase.dataPointDao().getAll()
                val dataPointScheduleIds = allDataPoints.map { it.scheduleId }.toSet()
                val (existing, nonExisting) = countsToStore.keys.partition { it in dataPointScheduleIds }

                existing.forEach { id ->
                    val existingEntity = allDataPoints.firstOrNull { it.scheduleId == id }
                    existingEntity?.let { entity ->
                        val updatedEntity =
                            entity.copy(count = entity.count + (countsToStore[id] ?: 0))
                        appDatabase.dataPointDao().update(updatedEntity)
                    }
                }

                nonExisting.forEach { id ->
                    val newEntity = DataPointEntity(
                        scheduleId = id,
                        count = countsToStore[id] ?: 0
                    )
                    appDatabase.dataPointDao().insert(newEntity)
                }
            }
        }
    }

    fun get(scheduleId: String): Flow<DataPointEntity?> {
        return appDatabase.dataPointDao().getByScheduleId(scheduleId)
    }

    fun delete(scheduleId: String) {
        StudyScope.launch {
            appDatabase.dataPointDao().deleteByScheduleId(scheduleId)
        }
    }
}