package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationDataRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

abstract class ObservationDataManager {
    private val mutex = Mutex()
    private val timeStampMutex = Mutex()
    private val observationDataRepository = ObservationDataRepository()
    private val observationRepository = ObservationRepository()
    private val dataPointCountRepository = DataPointCountRepository()
    private var countJob: String? = null
    private var scheduleCountJob: String? = null
    private var observationTimestampJob: String? = null

    private var scheduleCount = mutableMapOf<String, Long>()
    private var observationCollectionTimestamp = mutableMapOf<String, Long>()

    init {
        Napier.i(tag = "ObservationDataManager::init") { "ObservationDataManager init!" }
    }

    fun add(dataList: List<ObservationDataSchema>, scheduleIdList: Set<String>) {
        if (dataList.isNotEmpty()) {
            Napier.i(tag = "ObservationDataManager::add") { "Adding ${dataList.size} observations for schedule IDs: $scheduleIdList" }
            observationDataRepository.addData(dataList)
            if (scheduleCountJob == null) {
                listenToDatapointCountChanges()
            }
            Scope.launch(Dispatchers.Default) {
                mutex.withLock {
                    if (countJob == null) {
                        listenToDatapointCountChanges()
                    }
                    scheduleIdList.forEach {
                        scheduleCount[it] = scheduleCount.getOrElse(it) { 0 } + dataList.size
                    }
                }
            }
            Scope.launch {
                val now = Clock.System.now().epochSeconds
                val ids = dataList.map { it.observationId }.toSet()
                if (observationTimestampJob == null) {
                    listenToDatapointCountChanges()
                }
                timeStampMutex.withLock {
                    ids.forEach {
                        observationCollectionTimestamp[it] = now
                    }
                }
            }
        }
    }

    fun saveAndSend() {
        Napier.i(tag = "ObservationDataManager::saveAndSend") { "Saving and sending observations" }
        Scope.launch {
            observationDataRepository.store()
            observationDataRepository.count().firstOrNull()?.let {
                if (it in 1 until DATA_COUNT_THRESHOLD) {
                    sendData()
                }
            }
        }
    }

    fun store() {
        Napier.i(tag = "ObservationDataManager::store") { "Storing observations" }
        observationDataRepository.store()
        storeCount()
        storeTimestamps()
    }

    private fun storeCount() {
        if (scheduleCount.isNotEmpty()) {
            Napier.i(tag = "ObservationDataManager::storeCount") { "Storing count of observations" }
            Scope.launch {
                val copiedScheduleCount = mutex.withLock {
                    val copiedScheduleCount = scheduleCount.toMap()
                    scheduleCount = mutableMapOf()
                    copiedScheduleCount
                }
                copiedScheduleCount.forEach {
                    dataPointCountRepository.incrementCount(setOf(it.key), it.value)
                }
            }
        }
    }

    private fun storeTimestamps() {
        if (observationCollectionTimestamp.isNotEmpty()) {
            Napier.d(tag = "ObservationDataManager::storeTimestamps") { "Storing timestamps of observations" }
            Scope.launch {
                val copiedMap = timeStampMutex.withLock {
                    val copiedMap = observationCollectionTimestamp.toMap()
                    observationCollectionTimestamp = mutableMapOf()
                    copiedMap
                }
                copiedMap.forEach {
                    observationRepository.lastCollection(it.key, it.value)
                }
            }
        }
    }

    fun removeDataPointCount(scheduleId: String) {
        Napier.d(tag = "ObservationDataManager::removeDataPointCount") { "Removing datapoint count for schedule ID: $scheduleId" }
        scheduleCount.remove(scheduleId)
        observationCollectionTimestamp.remove(scheduleId)
    }

    abstract fun sendData(onCompletion: (Boolean) -> Unit = {})

    fun listenToDatapointCountChanges() {
        Napier.d(tag = "ObservationDataManager::listenToDatapointCountChanges") { "Starting to listen for changes in datapoint counts" }
        if (countJob == null || Scope.isActive(countJob!!)) {
            countJob = Scope.repeatedLaunch(10000) {
                observationDataRepository.count().cancellable().firstOrNull()?.let {
                    if (it > 0) {
                        Napier.d(tag = "ObservationDataManager::listenToDatapointCountChanges") { "Observation data count: $it! Sending data..." }
                        sendData()
                    }
                }
            }.first
        }
        if (scheduleCountJob == null || Scope.isActive(scheduleCountJob!!)) {
            scheduleCountJob = Scope.repeatedLaunch(5000) {
                storeCount()
            }.first
        }
        if (observationTimestampJob == null || Scope.isActive(observationTimestampJob!!)) {
            observationTimestampJob = Scope.repeatedLaunch(15000) {
                storeTimestamps()
            }.first
        }
    }

    fun stopListeningToCountChanges() {
        Napier.d(tag = "ObservationDataManager::stopListeningToCountChanges") { "Stopped listening for changes in datapoint counts" }
        Scope.cancel(setOf(countJob, scheduleCountJob, observationTimestampJob).filterNotNull())
        countJob = null
        scheduleCountJob = null
        observationTimestampJob = null
    }

    private fun deleteAll(idSet: Set<String>) {
        observationDataRepository.deleteAllWithId(idSet)
    }
}