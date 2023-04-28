package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationDataRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class ObservationDataManager() {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val observationDataRepository = ObservationDataRepository()
    private val observationRepository = ObservationRepository()
    private val dataPointCountRepository = DataPointCountRepository()
    private var isListeningToDataPointChanges = false

    fun add(dataList: List<ObservationDataSchema>, scheduleIdList: Set<String>) {
        if (dataList.isNotEmpty()) {
            Napier.d { "${dataList.size} new data points recorded for schedules: $scheduleIdList!" }
            listenToDatapointCountChanges()
            observationDataRepository.addMultiple(dataList)
            dataPointCountRepository.incrementCount(scheduleIdList, dataList.size.toLong())
            dataList.groupBy { it.observationType }
                .mapValues { it.value.maxBy { it.timestamp }.timestamp.epochSeconds }
                .forEach {
                    Napier.d { "Updating collection timestamp for ${it.key}..." }
                    observationRepository.lastCollection(it.key, it.value)
                }
        }
    }

    fun add(data: ObservationDataSchema, scheduleId: String) {
        Napier.d { "1 new data points recorded for schedule $scheduleId!" }
        listenToDatapointCountChanges()
        observationDataRepository.addData(data)
        dataPointCountRepository.incrementCount(setOf(scheduleId))
        Napier.d { "Updating collection timestamp for ${data.observationType}..." }
        observationRepository.lastCollection(data.observationType, data.timestamp.epochSeconds)
    }

    fun saveAndSend() {
        scope.launch {
            Napier.d { "Saving and sending data..." }
            observationDataRepository.storeAndQuery()?.let {
                if (it.dataPoints.isNotEmpty()) {
                    Napier.d { "Stored data, sending..." }
                    sendData()
                }
            }
        }
    }

    fun store() {
        Napier.d { "Storing observation data..." }
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
                    Napier.d { "Observation data count: $it" }
                    try {
                        if (it >= DATA_COUNT_THRESHOLD) {
                            Napier.d { "Sending $it data points..." }
                            sendData()
                        }
                    } finally {
                        Napier.d { "Coroutine closing! Storing and sending data..." }
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