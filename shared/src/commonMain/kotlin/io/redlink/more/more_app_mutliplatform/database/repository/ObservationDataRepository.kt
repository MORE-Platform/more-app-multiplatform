package io.redlink.more.more_app_mutliplatform.database.repository

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.mapAsBulkData
import io.redlink.more.more_app_mutliplatform.observations.QUEUE_COUNT_THRESHOLD
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ObservationDataRepository: Closeable {
    private val realmDatabase = DatabaseManager.database

    private val queue = mutableListOf<ObservationDataSchema>()
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    fun addData(data: ObservationDataSchema) {
        queue.add(data)
        if (queue.size >= QUEUE_COUNT_THRESHOLD) {
            storeDataFromQueue()
        }
    }

    fun databaseCount() = realmDatabase.count<ObservationDataSchema>()

    fun storeDataFromQueue() {
        val queueCopy = queue.toList()
        queue.clear()
        scope.launch {
            realmDatabase.storeAll(queueCopy)
        }
    }

    suspend fun storeAndQuery(): DataBulk? {
        val queueCopy = queue.toList()
        queue.clear()
        realmDatabase.storeAll(queueCopy)
        return allAsBulk()
    }

    suspend fun allAsBulk(): DataBulk? {
        return realmDatabase.query<ObservationDataSchema>().firstOrNull()?.mapAsBulkData()
    }

    fun deleteAllWithId(idSet: Set<String>) {
        realmDatabase.deleteAllWhereFieldInList<ObservationDataSchema>("dataId", list = idSet)
    }

    override fun close() {
        realmDatabase.close()
    }
}