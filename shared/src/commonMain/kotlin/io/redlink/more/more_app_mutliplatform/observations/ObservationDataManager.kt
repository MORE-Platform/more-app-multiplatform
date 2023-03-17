package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationDataRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData
import kotlinx.coroutines.*

private const val TAG = "ObservationDataManager"

abstract class ObservationDataManager(private val networkService: NetworkService): Closeable {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val observationDataRepository = ObservationDataRepository()
    init {
        scope.launch {
            observationDataRepository.count().collect{
                Napier.i(tag = TAG) {"Current collected data count: $it"}
                if (it >= DATA_COUNT_THRESHOLD) {
                    sendData()
                }
            }
        }
    }

    fun add(dataList: List<ObservationDataSchema>) {
        observationDataRepository.addMultiple(dataList)
        dataList.forEach { Napier.i(tag = TAG) { "New data recorded: $it" } }
    }

    fun add(data: ObservationDataSchema) {
        Napier.i(tag = TAG) { "New data recorded: $data" }
        observationDataRepository.addData(data)
    }

    fun saveAndSend() {
        scope.launch {
            observationDataRepository.storeAndQuery()?.let {
                sendData()
            }
        }
    }

    abstract fun sendData()

    private fun deleteAll(idSet: Set<String>) {
        observationDataRepository.deleteAllWithId(idSet)
    }

    override fun close() {
        observationDataRepository.close()
    }
}