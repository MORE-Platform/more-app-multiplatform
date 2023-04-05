package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationDataRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "ObservationDataManager"

abstract class ObservationDataManager(): Closeable {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val observationDataRepository = ObservationDataRepository()
    private val dataPointCountRepository = DataPointCountRepository()

    init {
        scope.launch {
            observationDataRepository.count().collect {
                Napier.i(tag = TAG) { "Current collected data count: $it" }
                if (it >= DATA_COUNT_THRESHOLD) {
                    sendData()
                }
            }
        }
    }

    fun add(dataList: List<ObservationDataSchema>, scheduleIdList: Set<String>) {
        if (dataList.isNotEmpty()) {
            observationDataRepository.addMultiple(dataList)
            dataPointCountRepository.incrementCount(scheduleIdList, dataList.size.toLong())
        }
    }

    fun add(data: ObservationDataSchema, scheduleId: String) {
        Napier.i(tag = TAG) { "New data recorded: $data" }
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

    private suspend fun deleteAll(idSet: Set<String>) {
        observationDataRepository.deleteAllWithId(idSet)
    }

    override fun close() {
    }
}