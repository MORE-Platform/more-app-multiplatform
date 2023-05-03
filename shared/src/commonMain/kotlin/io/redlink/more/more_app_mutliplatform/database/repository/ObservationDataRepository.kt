package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.mapAsBulkData
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import kotlinx.coroutines.flow.firstOrNull
import org.mongodb.kbson.ObjectId

class ObservationDataRepository: Repository<ObservationDataSchema>() {

    override val repositoryName: String
        get() = "ObservationDataRepository"

    private val queue = mutableListOf<ObservationDataSchema>()

    fun addData(data: ObservationDataSchema) {
//        queue.add(data)
//        if (queue.size > QUEUE_THRESHOLD) {
//            store()
//        }
        realmDatabase().store(setOf(data), UpdatePolicy.ERROR)
    }

    fun addData(dataList: List<ObservationDataSchema>) {
//        queue.addAll(dataList)
//        if (queue.size > QUEUE_THRESHOLD) {
//            store()
//        }
        realmDatabase().store(dataList, UpdatePolicy.ERROR)
    }

    fun store() {
        if (queue.isNotEmpty()) {
            val queueCopy = queue.toSet()
            queue.clear()
            realmDatabase().store(queueCopy, UpdatePolicy.ERROR)
        }
    }

    override fun count() = realmDatabase().count<ObservationDataSchema>()


    suspend fun allAsBulk(): DataBulk? {
        return realmDatabase().query<ObservationDataSchema>(limit = 10000).firstOrNull()?.mapAsBulkData()
    }


    fun deleteAllWithId(idSet: Set<String>) {
        Napier.d { "Deleting ${idSet.size} elements..." }
        val objectIdSet = idSet.map { ObjectId(it) }.toSet()
        realm()?.writeBlocking {
            this.query<ObservationDataSchema>().find().filter { it.dataId in objectIdSet }.forEach {
                delete(it)
            }
        }
    }

    companion object {
        private const val QUEUE_THRESHOLD = 10
    }
}