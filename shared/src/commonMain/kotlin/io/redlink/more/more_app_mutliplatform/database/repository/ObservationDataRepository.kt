package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.mapAsBulkData
import io.redlink.more.more_app_mutliplatform.observations.QUEUE_COUNT_THRESHOLD
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import kotlinx.coroutines.flow.firstOrNull
import org.mongodb.kbson.ObjectId

class ObservationDataRepository: Repository<ObservationDataSchema>() {

    private val queue = mutableListOf<ObservationDataSchema>()

    fun addData(data: ObservationDataSchema) {
        Napier.d { "Adding new data: $data" }
        queue.add(data)
        Napier.d { "Queue size: ${queue.size}" }
        if (queue.size >= QUEUE_COUNT_THRESHOLD) {
            storeDataFromQueue()
        }
    }

    fun addMultiple(dataList: List<ObservationDataSchema>) {
        Napier.d { "Adding ${dataList.size} points to queue" }
        queue.addAll(dataList)
        Napier.d { "Queue size: ${queue.size}" }
        if (queue.size >= QUEUE_COUNT_THRESHOLD) {
            storeDataFromQueue()
        }
    }

    override fun count() = realmDatabase.count<ObservationDataSchema>()

    fun storeDataFromQueue() {
        if (queue.isNotEmpty()) {
            val queueCopy = queue.toList()
            queue.clear()
            Napier.d { "Storing data queue with ${queueCopy.size} elements..." }
            realmDatabase.store(queueCopy)
        }
    }

    suspend fun storeAndQuery(): DataBulk? {
        if (queue.isNotEmpty()) {
            val queueCopy = queue.toList()
            queue.clear()
            Napier.d { "Storing data queue with ${queueCopy.size} elements..." }
            realmDatabase.store(queueCopy)
        }
        return allAsBulk()
    }

    suspend fun allAsBulk(): DataBulk? {
        return realmDatabase.query<ObservationDataSchema>().firstOrNull()?.mapAsBulkData()
    }

    suspend fun deleteAllWithId(idSet: Set<String>) {
        Napier.d { "Deleting ${idSet.size} elements with ID: $idSet" }
        realmDatabase.deleteAllWhereFieldInList<ObservationDataSchema>("dataId", idSet.map { ObjectId(it) })
    }
}