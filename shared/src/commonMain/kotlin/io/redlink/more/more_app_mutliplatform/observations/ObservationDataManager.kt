package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationDataRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.repeatEveryFewSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

abstract class ObservationDataManager {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val observationDataRepository = ObservationDataRepository()
    private val observationRepository = ObservationRepository()
    private val dataPointCountRepository = DataPointCountRepository()

    init {
        listenToDatapointCountChanges()
    }

    fun add(dataList: List<ObservationDataSchema>, scheduleIdList: Set<String>) {
        if (dataList.isNotEmpty()) {
            observationDataRepository.addData(dataList)
            scope.launch(Dispatchers.Default) {
                dataPointCountRepository.incrementCount(scheduleIdList, dataList.size.toLong())
                dataList.groupBy { it.observationType }
                    .mapValues { it.value.maxBy { it.timestamp }.timestamp.epochSeconds }
                    .forEach {
                        observationRepository.lastCollection(it.key, it.value)
                    }
            }
        }
    }

    fun add(data: ObservationDataSchema, scheduleId: String) {
        observationDataRepository.addData(data)
        scope.launch(Dispatchers.Default) {
            dataPointCountRepository.incrementCount(setOf(scheduleId))
            observationRepository.lastCollection(data.observationType, data.timestamp.epochSeconds)
        }
    }

    fun saveAndSend() {
        scope.launch {
            observationDataRepository.store()
            observationDataRepository.count().firstOrNull()?.let {
                if (it in 1 until DATA_COUNT_THRESHOLD) {
                    sendData()
                }
            }
        }
    }

    fun store() {
        observationDataRepository.store()
    }

    fun removeDataPointCount(scheduleId: String) {
        dataPointCountRepository.delete(scheduleId)
    }

    abstract fun sendData(onCompletion: (Boolean) -> Unit = {})

    private fun listenToDatapointCountChanges() {
        Napier.d { "Creating listener for datapoints..." }
        scope.repeatEveryFewSeconds(10000) {
            Napier.d { "Looking for new data..." }
            observationDataRepository.count().firstOrNull()?.let {
                Napier.d { "Datapoint count: $it" }
                if (it > 0) {
                    Napier.d { "Observation data count: $it! Sending data..." }
                    sendData()
                }
            }
        }
//        if (collectionJob != null && collectionJob?.isActive == false) {
////            scope.launch {
////                observationDataRepository.count().collect {
////                    if (it >= DATA_COUNT_THRESHOLD) {
////                        Napier.d { "Observation data count: $it! Sending data..." }
////                        sendData()
////                    }
////                }
////
////            }
//        }
    }

    private fun deleteAll(idSet: Set<String>) {
        observationDataRepository.deleteAllWithId(idSet)
    }

    companion object {
    }
}