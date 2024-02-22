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
import io.redlink.more.more_app_mutliplatform.util.Scope.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DataPointCountRepository : Repository<DataPointCountSchema>() {
    private val mutex = Mutex()
    override fun count(): Flow<Long> {
        return realmDatabase().count<DataPointCountSchema>()
    }

    fun incrementCount(scheduleIdSet: Set<String>, addCount: Long = 1) {
        if (scheduleIdSet.isNotEmpty()) {
            launch {
                mutex.withLock {
                    realm()?.write {
                        val dataPointCounts = this.query<DataPointCountSchema>()
                            .find()
                            .filter { it.scheduleId in scheduleIdSet }
                        val dataPointScheduleIds = dataPointCounts.map { it.scheduleId }.toSet()
                        val (existing, nonExisting) = scheduleIdSet.partition { it in dataPointScheduleIds}
                        existing.map { id ->
                            dataPointCounts.firstOrNull { it.scheduleId == id }?.let {
                                it.count += addCount
                            }
                        }
                        nonExisting.map { id ->
                            this.copyToRealm(DataPointCountSchema().apply {
                                count = addCount
                                this.scheduleId = id
                            })
                        }
                    }
                }
            }
        }
    }

    fun get(scheduleId: String): Flow<DataPointCountSchema?> {
        return realmDatabase().queryFirst(query = "scheduleId == $0", queryArgs = arrayOf(scheduleId))
    }

    fun delete(scheduleId: String) {
        realm()?.writeBlocking {
            val existingObject: DataPointCountSchema? = this.query<DataPointCountSchema>(
                query = "scheduleId == $0", scheduleId).first().find()
            existingObject?.let {
                this.delete(it)
            }
        }
    }
}