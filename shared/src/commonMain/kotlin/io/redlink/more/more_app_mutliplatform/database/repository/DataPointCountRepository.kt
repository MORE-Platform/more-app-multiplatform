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

import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DataPointCountRepository : Repository<DataPointCountSchema>() {
    private val mutex = Mutex()
    private val countQueue = mutableMapOf<String, Long>()
    private var storeJob: Job? = null

    override fun count(): Flow<Long> {
        return realmDatabase().count<DataPointCountSchema>()
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
                    storeJob = StudyScope.repeatedLaunch(5000L) {
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
            StudyScope.launch {
                realm()?.write {
                    val dataPointCounts = this.query<DataPointCountSchema>().find()
                    val dataPointScheduleIds = dataPointCounts.map { it.scheduleId }.toSet()
                    val (existing, nonExisting) = countsToStore.keys.partition { it in dataPointScheduleIds }
                    existing.forEach { id ->
                        dataPointCounts.firstOrNull { it.scheduleId == id }?.let {
                            it.count += countsToStore[id] ?: 0
                        }
                    }
                    nonExisting.forEach { id ->
                        this.copyToRealm(DataPointCountSchema().apply {
                            count = countsToStore[id] ?: 0
                            this.scheduleId = id
                        })
                    }
                }
            }
        }
    }

    fun get(scheduleId: String): Flow<DataPointCountSchema?> {
        return realmDatabase().queryFirst(
            query = "scheduleId == $0",
            queryArgs = arrayOf(scheduleId)
        )
    }

    fun delete(scheduleId: String) {
        realm()?.writeBlocking {
            val existingObject: DataPointCountSchema? = this.query<DataPointCountSchema>(
                query = "scheduleId == $0", scheduleId
            ).first().find()
            existingObject?.let {
                this.delete(it)
            }
        }
    }
}