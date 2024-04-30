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

import io.github.aakira.napier.Napier
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.mapAsBulkData
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mongodb.kbson.ObjectId

class ObservationDataRepository : Repository<ObservationDataSchema>() {
    private var queue = mutableSetOf<ObservationDataSchema>()
    private val mutex = Mutex()

    fun addData(dataList: List<ObservationDataSchema>) {
        StudyScope.launch {
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
            StudyScope.launch {
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
            realmDatabase().query<ObservationDataSchema>(limit = 5000).firstOrNull()
                ?.mapAsBulkData()
        }
    }

    fun allAsBulk(completionHandler: (DataBulk?) -> Unit) {
        StudyScope.launch(Dispatchers.IO) {
            allAsBulk()?.let { completionHandler(it) }
        }
    }

    fun deleteAllWithId(idSet: Set<String>) {
        Napier.i { "Deleting ${idSet.size} elements..." }
        val objectIdSet = idSet.map { ObjectId(it) }.toSet()
        StudyScope.launch {
            mutex().withLock {
                realm()?.write {
                    this.query<ObservationDataSchema>().find().filter { it.dataId in objectIdSet }
                        .forEach {
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