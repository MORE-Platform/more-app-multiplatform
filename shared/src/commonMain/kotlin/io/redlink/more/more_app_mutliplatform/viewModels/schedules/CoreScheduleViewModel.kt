package io.redlink.more.more_app_mutliplatform.viewModels.schedules

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.extensions.time
import io.redlink.more.more_app_mutliplatform.extensions.toLocalDate
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CoreScheduleViewModel(private val dataRecorder: DataRecorder) {
    private val observationRepository = ObservationRepository()
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private var listJob: Job? = null

    val scheduleModelList: MutableStateFlow<Map<Long, List<ScheduleModel>>> =
        MutableStateFlow( emptyMap())

    init {
        listJob = createListCollectingJob()
    }

    fun reinitList() {
        listJob?.cancel()
        listJob = createListCollectingJob()
    }

    private fun createListCollectingJob() = scope.launch {
        observationRepository.observationWithUndoneSchedules().collect { observationList ->
            scheduleModelList.value = createMap(observationList)
        }
    }

    fun reloadData() {
        scope.launch {
            scheduleModelList.firstOrNull()?.let { map ->
                val currentTime = Clock.System.now().toEpochMilliseconds()
                scheduleModelList.emit(map.mapValues { entry ->
                    entry.value.filter { it.end > currentTime && !it.done }
                }.filter { entry -> entry.value.isNotEmpty() })
            }
        }
    }

    fun removeSchedule(scheduleId: String) {
        stop(scheduleId)
        scope.launch {
            scheduleModelList.firstOrNull()?.let { map ->
                val currentTime = Clock.System.now().toEpochMilliseconds()
                scheduleModelList.emit(map.mapValues { entry ->
                    entry.value.filter { it.end > currentTime && !it.done }
                        .filterNot { it.scheduleId == scheduleId }
                }.filter { entry -> entry.value.isNotEmpty() })
            }
        }
    }

    fun start(scheduleId: String) {
        dataRecorder.start(scheduleId)
    }

    fun pause(scheduleId: String) {
        dataRecorder.pause(scheduleId)
    }

    fun stop(scheduleId: String) {
        dataRecorder.stop(scheduleId)
    }

    private fun initializeDataCount(scheduleId: String): DataPointCountSchema {
        return DataPointCountSchema().apply {
            this.count = 0
            this.scheduleId = scheduleId
        }
    }

    private fun createMap(observationList: List<ObservationSchema>): Map<Long, List<ScheduleModel>> {
        return observationList
            .asSequence()
            .map { ScheduleModel.createModelsFrom(it) }
            .flatten()
            .filter { it.end > Clock.System.now().toEpochMilliseconds() && !it.done }
            .sortedBy { it.start }
            .groupBy { it.start.toLocalDate().time() }
    }

    fun onScheduleModelListChange(provideNewState: ((Map<Long, List<ScheduleModel>>) -> Unit)): Closeable {
        val job = Job()
        scheduleModelList.onEach {
            provideNewState(it)
        }.launchIn(CoroutineScope(Dispatchers.Main + job))
        return object: Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }
}

