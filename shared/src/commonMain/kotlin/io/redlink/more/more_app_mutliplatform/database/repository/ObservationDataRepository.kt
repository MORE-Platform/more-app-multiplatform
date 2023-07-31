package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.mapAsBulkData
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.util.Scope.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mongodb.kbson.ObjectId

class ObservationDataRepository: Repository<ObservationDataSchema>() {
    private var queue = mutableSetOf<ObservationDataSchema>()
    private val mutex = Mutex()

    fun addData(dataList: List<ObservationDataSchema>) {
        launch {
            mutex.withLock {
                queue.addAll(dataList)
            }
            if (queue.size > QUEUE_THRESHOLD) {
                store()
            }
        }
    }

    fun store() {
        if (queue.isNotEmpty()) {
            launch {
                val queueCopy = mutex.withLock {
                    val queueCopy = queue.toSet()
                    queue = mutableSetOf()
                    queueCopy
                }
                realmDatabase().store(queueCopy, UpdatePolicy.ERROR)
            }
        }
    }

    override fun count() = realmDatabase().count<ObservationDataSchema>()


    suspend fun allAsBulk(): DataBulk? {
        return mutex().withLock {
            realmDatabase().query<ObservationDataSchema>(limit = 5000).firstOrNull()?.mapAsBulkData()
        }
    }

    fun allAsBulk(completionHandler: (DataBulk?) -> Unit) {
        CoroutineScope(Job() + Dispatchers.Default).launch {
            allAsBulk()?.let { completionHandler(it) }
        }
    }

    fun deleteAllWithId(idSet: Set<String>) {
        Napier.d { "Deleting ${idSet.size} elements..." }
        val objectIdSet = idSet.map { ObjectId(it) }.toSet()
        launch {
            mutex().withLock {
                realm()?.write {
                    this.query<ObservationDataSchema>().find().filter { it.dataId in objectIdSet }.forEach {
                        delete(it)
                    }
                }
            }
        }
    }

    companion object {
        private const val QUEUE_THRESHOLD = 10
    }
}