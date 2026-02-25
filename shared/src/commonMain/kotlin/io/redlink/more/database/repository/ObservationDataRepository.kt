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

import io.github.aakira.napier.Napier
import io.redlink.more.database.AppDatabase
import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.extensions.mapAsBulkData
import io.redlink.more.scopes.Scope
import io.redlink.more.services.network.openapi.model.DataBulk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ObservationDataRepository(private val appDatabase: AppDatabase) {
    private var queue = mutableSetOf<ObservationDataEntity>()
    private val mutex = Mutex()

    init {
        Scope.repeatedLaunch(10000L, Dispatchers.IO) {
            if (queue.isNotEmpty()) {
                store()
            }
        }
    }

    fun addData(dataList: List<ObservationDataEntity>) {
        Scope.launch {
            mutex.withLock {
                queue.addAll(dataList)
            }
        }
    }

    suspend fun store() {
        if (queue.isNotEmpty()) {
            val queueCopy = mutex.withLock {
                val queueCopy = queue.toSet()
                queue.clear()
                queueCopy
            }
            appDatabase.observationDataDao().insertAll(queueCopy.toList())
        }
    }

    suspend fun getCount(): Int = appDatabase.observationDataDao().getCount()

    suspend fun allAsBulk(): io.redlink.more.model.DataBulk? {
        return mutex.withLock {
            val observationDataEntities = appDatabase.observationDataDao().getLatest(5000)
            if (observationDataEntities.isNotEmpty()) {
                observationDataEntities.mapAsBulkData()
            } else {
                null
            }
        }
    }

    suspend fun deleteAllWithId(idSet: Set<String>) {
        Napier.i { "Deleting ${idSet.size} elements..." }
        mutex.withLock {
            idSet.forEach { dataId ->
                appDatabase.observationDataDao().deleteById(dataId)
            }
        }
    }
}