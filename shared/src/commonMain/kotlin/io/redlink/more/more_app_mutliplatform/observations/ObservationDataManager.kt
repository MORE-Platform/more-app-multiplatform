package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.extensions.mapAsBulkData
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "ObservationDataManager"

class ObservationDataManager {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private var networkService: NetworkService? = null

    private val dataQueue = mutableListOf<ObservationData>()

    init {
        scope.launch {

        }
    }

    fun setNetworkService(networkService: NetworkService) {
        this.networkService = networkService
    }

    fun add(data: ObservationData) {
        dataQueue.add(data)
    }

    private fun storeQueueToDB() {
        if (dataQueue.size >= QUEUE_COUNT_THRESHOLD) {

        }
    }

    fun sendRecordedData(data: Set<ObservationData>) {
        networkService?.let { networkService ->
            data.mapAsBulkData()?.let {
                scope.launch {
                    val (idList, error) = networkService.sendData(it)
                    if (idList.isNotEmpty()) {
                        deleteAll(idList)
                    }
                    error?.let {
                        Napier.e(tag = TAG, message = it.message)
                    }
                }
            }
        }
    }

    private fun getAllDataRecords(): Set<ObservationData> {
        return emptySet()
    }

    private fun deleteAll(idList: Set<String>) {

    }
}