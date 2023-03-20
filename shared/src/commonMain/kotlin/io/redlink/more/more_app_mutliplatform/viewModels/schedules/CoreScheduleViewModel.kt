package io.redlink.more.more_app_mutliplatform.viewModels.schedules

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.extensions.*
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock

class CoreScheduleViewModel(private val observationFactory: ObservationFactory) {
    private val observationRepository = ObservationRepository()
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    val scheduleModelList: MutableStateFlow<Map<Long, List<ScheduleModel>>> =
        MutableStateFlow(emptyMap())

    val activeScheduleState: MutableStateFlow<Map<String, ScheduleState>> = MutableStateFlow(
        emptyMap()
    )

    private val observationMap = mutableMapOf<String, Observation>()

    init {
        scope.launch {
            observationRepository.observationWithUndoneSchedules().collect { observationList ->
                scheduleModelList.value = createMap(observationList)
            }
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

    fun start(scheduleModel: ScheduleModel) {
        Napier.i { "Trying to start ${scheduleModel.scheduleId}" }
        if (observationMap[scheduleModel.scheduleId] != null && observationMap[scheduleModel.scheduleId]?.start(scheduleModel.observationId) == true) {
            setObservationState(scheduleModel.scheduleId, ScheduleState.RUNNING)
        } else {
            observationFactory.observation(id = scheduleModel.observationId, type = scheduleModel.observationType, config = scheduleModel.config, scheduleId = scheduleModel.scheduleId)?.let {
                observationMap[scheduleModel.scheduleId] = it
                if (it.start(scheduleModel.observationId)) {
                    Napier.i { "Recording started of ${scheduleModel.scheduleId}" }
                    setObservationState(scheduleModel.scheduleId, ScheduleState.RUNNING)
                }
            }
        }
    }

    fun pause(scheduleId: String) {
        observationMap[scheduleId]?.let {
            stopSensor(it)
            setObservationState(scheduleId, ScheduleState.PAUSED)
        }
    }

    fun stop(scheduleId: String) {
        observationMap[scheduleId]?.let {
            stopSensor(it)
            setObservationState(scheduleId, ScheduleState.STOPPED)
            observationMap.remove(scheduleId)
        }
    }

    private fun initializeDataCount(scheduleId: String): DataPointCountSchema {
        return DataPointCountSchema().apply {
            this.count = 0
            this.scheduleId = scheduleId
        }
    }

    private fun setObservationState(scheduleId: String, state: ScheduleState) {
        scope.launch {
            activeScheduleState.firstOrNull()?.let {
                val mutable = it.toMutableMap()
                mutable[scheduleId] = state
                activeScheduleState.emit(mutable)
            }
        }
    }

    private fun stopSensor(observation: Observation) {
        observation.stop()
        observation.finish()
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
        return scheduleModelList.asClosure(provideNewState)
    }

    fun onScheduleStateChange(provideNewState: (Map<String, ScheduleState>) -> Unit): Closeable {
        return activeScheduleState.asClosure(provideNewState)
    }
}

