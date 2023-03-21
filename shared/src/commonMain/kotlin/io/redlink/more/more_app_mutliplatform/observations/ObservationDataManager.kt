package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationDataRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "ObservationDataManager"

abstract class ObservationDataManager: Closeable {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val observationDataRepository = ObservationDataRepository()
    private val dataPointCountRepository: DataPointCountRepository = DataPointCountRepository()
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

    fun add(dataList: List<ObservationDataSchema>, scheduleIdList: Set<String>) {
        observationDataRepository.addMultiple(dataList)
        dataList.forEach { Napier.i(tag = TAG) { "New data recorded: $it" } }
        scheduleIdList.forEach {
            dataPointCountRepository.incrementCount(it)
        }
    }

    fun add(data: ObservationDataSchema, scheduleId: String) {
        Napier.i(tag = TAG) { "New data recorded: $data" }
        observationDataRepository.addData(data)
        dataPointCountRepository.incrementCount(scheduleId)
    }

    fun saveAndSend() {
        scope.launch {
            observationDataRepository.storeAndQuery()?.let {
                sendData()
            }
        }
    }

    fun removeDataPointCount(scheduleId: String) {
        dataPointCountRepository.delete(scheduleId)
    }

    abstract fun sendData()

    private fun deleteAll(idSet: Set<String>) {
        observationDataRepository.deleteAllWithId(idSet)
    }

    override fun close() {
        observationDataRepository.close()
    }
}