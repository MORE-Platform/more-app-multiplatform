package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationDataRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "ObservationDataManager"

abstract class ObservationDataManager() {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val observationDataRepository = ObservationDataRepository()
    private val dataPointCountRepository = DataPointCountRepository()
    private var isListeningToDataPointChanges = false

    fun add(dataList: List<ObservationDataSchema>, scheduleIdList: Set<String>) {
        if (dataList.isNotEmpty()) {
            listenToDatapointCountChanges()
            observationDataRepository.addMultiple(dataList)
            dataPointCountRepository.incrementCount(scheduleIdList, dataList.size.toLong())
        }
    }

    fun add(data: ObservationDataSchema, scheduleId: String) {
        Napier.i(tag = TAG) { "New data recorded: $data" }
        listenToDatapointCountChanges()
        observationDataRepository.addData(data)
        dataPointCountRepository.incrementCount(setOf(scheduleId))
    }

    fun saveAndSend() {
        scope.launch {
            observationDataRepository.storeAndQuery()?.let {
                if (it.dataPoints.isNotEmpty()) {
                    sendData()
                }
            }
        }
    }

    fun store() {
        observationDataRepository.storeDataFromQueue()
    }

    fun removeDataPointCount(scheduleId: String) {
        dataPointCountRepository.delete(scheduleId)
    }

    abstract fun sendData(onCompletion: (Boolean) -> Unit = {})

    private fun listenToDatapointCountChanges() {
        if (!isListeningToDataPointChanges) {
            isListeningToDataPointChanges = true
            scope.launch {
                observationDataRepository.count().collect {
                    try {
                        Napier.i(tag = TAG) { "Current collected data count: $it" }
                        if (it >= DATA_COUNT_THRESHOLD) {
                            sendData()
                        }
                    } finally {
                        store()
                        sendData()
                    }
                }
            }
        }
    }

    private suspend fun deleteAll(idSet: Set<String>) {
        observationDataRepository.deleteAllWithId(idSet)
    }
}