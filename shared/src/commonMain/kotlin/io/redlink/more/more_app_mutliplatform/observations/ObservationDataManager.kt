package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationDataRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData
import kotlinx.coroutines.*

private const val TAG = "ObservationDataManager"

class ObservationDataManager(private val networkService: NetworkService): Closeable {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val observationDataRepository = ObservationDataRepository()
    init {
        scope.launch {
            observationDataRepository.count().collect{
                if (it >= DATA_COUNT_THRESHOLD) {
                    observationDataRepository.allAsBulk()?.let { dataBulk ->
                        sendRecordedData(dataBulk)
                    }
                }
            }
        }
    }

    fun add(data: ObservationDataSchema) {
        observationDataRepository.addData(data)
    }

    fun saveAndSend() {
        scope.launch {
            observationDataRepository.storeAndQuery()?.let {
                sendRecordedData(it)
            }
        }
    }

    private fun sendRecordedData(data: DataBulk) {
        scope.launch {
            val (idList, error) = networkService.sendData(data)
            if (idList.isNotEmpty()) {
                deleteAll(idList)
            }
            error?.let {
                Napier.e(tag = TAG, message = it.message)
            }
        }
    }

    private fun deleteAll(idSet: Set<String>) {
        observationDataRepository.deleteAllWithId(idSet)
    }

    override fun close() {
        observationDataRepository.close()
    }
}