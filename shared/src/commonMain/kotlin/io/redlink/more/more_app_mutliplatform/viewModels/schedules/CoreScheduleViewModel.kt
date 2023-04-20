package io.redlink.more.more_app_mutliplatform.viewModels.schedules

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.localDateTime
import io.redlink.more.more_app_mutliplatform.extensions.time
import io.redlink.more.more_app_mutliplatform.extensions.toLocalDate
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CoreScheduleViewModel(private val dataRecorder: DataRecorder) {
    private val observationRepository = ObservationRepository()
    private val scheduleRepository = ScheduleRepository()
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    val scheduleModelList: MutableStateFlow<Map<Long, List<ScheduleModel>>> =
        MutableStateFlow( emptyMap())

    val runningScheduleModelList: MutableStateFlow<MutableMap<Long, List<ScheduleModel>>> =
        MutableStateFlow(mutableMapOf())
    val completedScheduleModelList: MutableStateFlow<MutableMap<Long, List<ScheduleModel>>> =
        MutableStateFlow(mutableMapOf())

    init {
        scope.launch {
            scheduleRepository.allSchedulesWithStatus()
                .combine(observationRepository.observations()){ schedules, observations ->
                    observations.associate { observation ->
                        observation.observationTitle to schedules
                            .filter { it.observationId == observation.observationId } }
                }.collect {
                    scheduleModelList.emit(createMap(it))
                }
        }
    }

    fun reloadData() {
        scope.launch {
            scheduleModelList.firstOrNull()?.let { map ->
                val currentTime = Clock.System.now().toEpochMilliseconds()
                scheduleModelList.emit(map.mapValues { entry ->
                    entry.value.filter { it.end > currentTime && !it.done }
                }
                    .filterKeys { it >= Clock.System.now().localDateTime().date.time() }
                    .filterValues { it.isNotEmpty() })
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

    private fun createMap(observationList: Map<String, List<ScheduleSchema>>): Map<Long, List<ScheduleModel>> {
        val map =  observationList
            .asSequence()
            .map { ScheduleModel.createModelsFrom(it.key, it.value) }
            .flatten()
            .filter {
                        it.end > Clock.System.now().toEpochMilliseconds()
                        && !it.done
                        && it.scheduleState.active()
                                || it.scheduleState == ScheduleState.DEACTIVATED
            }
            .sortedBy { it.start }
            .groupBy {
                if(it.start <= Clock.System.now().toEpochMilliseconds()) {
                    Clock.System.now().localDateTime().date.time()
                }
                else { it.start.toLocalDate().time() }
            }
            .filterKeys { it >= Clock.System.now().localDateTime().date.time() }
            .filterValues { it.isNotEmpty() }
        filterRunningAndCompleted(map)
        return map
    }

    private fun filterRunningAndCompleted(map: Map<Long, List<ScheduleModel>>) {
        runningScheduleModelList.value.clear()
        completedScheduleModelList.value.clear()
        map.forEach { (key, values) ->
            val runningSchedules = values.filter { it.scheduleState == ScheduleState.RUNNING }
            val completedSchedules = values.filter { it.scheduleState == ScheduleState.DONE || it.scheduleState == ScheduleState.ENDED }
            if (runningSchedules.isNotEmpty()) {
                runningScheduleModelList.value[key] = runningSchedules
            }
            if (completedSchedules.isNotEmpty()) {
                completedScheduleModelList.value[key] = completedSchedules
            }
        }
        Napier.i { "Running schedules: ${runningScheduleModelList.value}" }
    }

    fun onScheduleModelListChange(provideNewState: (Map<Long, List<ScheduleModel>>) -> Unit): Closeable {
        return scheduleModelList.asClosure(provideNewState)
    }

    fun getRunningSchedules(provideNewState: (Map<Long, List<ScheduleModel>>) -> Unit): Closeable {
        return runningScheduleModelList.asClosure(provideNewState)
    }

    fun getCompletedSchedules(provideNewState: (Map<Long, List<ScheduleModel>>) -> Unit): Closeable {
        return completedScheduleModelList.asClosure(provideNewState)
    }
}

