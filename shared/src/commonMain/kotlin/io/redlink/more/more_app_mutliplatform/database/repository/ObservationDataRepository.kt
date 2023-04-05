package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.mapAsBulkData
import io.redlink.more.more_app_mutliplatform.observations.QUEUE_COUNT_THRESHOLD
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId

class ObservationDataRepository: Repository<ObservationDataSchema>() {

    private val queue = mutableListOf<ObservationDataSchema>()

    fun addData(data: ObservationDataSchema) {
        Napier.i { "Adding new data: $data" }
        queue.add(data)
        if (queue.size >= QUEUE_COUNT_THRESHOLD) {
            storeDataFromQueue()
        }
    }

    fun addMultiple(dataList: List<ObservationDataSchema>) {
        Napier.i { "Adding ${dataList.size} points to queue" }
        queue.addAll(dataList)
        if (queue.size >= QUEUE_COUNT_THRESHOLD) {
            storeDataFromQueue()
        }
    }

    override fun count() = realmDatabase.count<ObservationDataSchema>()

    fun storeDataFromQueue() {
        val queueCopy = queue.toList()
        queue.clear()
        realmDatabase.storeAll(queueCopy)
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

    suspend fun deleteAllWithId(idSet: Set<String>) {
        realmDatabase.deleteAllWhereFieldInList<ObservationDataSchema>("dataId", idSet.map { ObjectId(it) })
    }
}