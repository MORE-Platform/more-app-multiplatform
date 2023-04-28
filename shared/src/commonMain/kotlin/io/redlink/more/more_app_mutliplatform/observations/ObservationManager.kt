package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class ObservationManager(private val observationFactory: ObservationFactory) {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val scheduleRepository = ScheduleRepository()
    private val dataPointCountRepository = DataPointCountRepository()
    private val observationRepository = ObservationRepository()

    private val runningObservations = mutableMapOf<ScheduleSchema, Observation>()
    init {
        scope.launch {
            updateTaskStates()
            scheduleRepository.allSchedulesWithStatus(true).collect { list ->
                list.filter { it in runningObservations.keys }.forEach {
                    stop(it.observationId)
                    dataPointCountRepository.delete(it.scheduleId.toHexString())
                }
            }
        }
    }

    suspend fun restartStillRunning(): Set<String> {
        val startedObservations = mutableSetOf<String>()
        scheduleRepository.allScheduleWithRunningState().firstOrNull()?.let { list ->
            list.filter { it !in runningObservations.keys }.forEach {
                if (start(it.scheduleId.toHexString(), false)){
                    startedObservations.add(it.scheduleId.toHexString())
                }
            }
        }
        return startedObservations
    }

    suspend fun start(scheduleId: String, setCollectionTimestampToNow: Boolean = true): Boolean {
        Napier.d { "Trying to start $scheduleId..." }
        return findOrCreateObservation(scheduleId)?.let { scheduleSchema ->
            observationRepository.getObservationByObservationId(scheduleSchema.observationId)
                ?.let { observation ->
                    val config: MutableMap<String, Any> =
                        (observation.configuration?.let { config ->
                            try {
                                Json.decodeFromString<JsonObject>(config).toMap()
                            } catch (e: Exception) {
                                Napier.e { e.stackTraceToString() }
                                emptyMap()
                            }
                        } ?: emptyMap()).toMutableMap()
                    scheduleSchema.start?.let {
                        config[Observation.CONFIG_TASK_START] = it.epochSeconds
                    }
                    scheduleSchema.end?.let {
                        config[Observation.CONFIG_TASK_STOP] = it.epochSeconds
                    }
                    config[Observation.SCHEDULE_ID] =
                        scheduleSchema.scheduleId.toHexString()
                    start(scheduleSchema, config, setCollectionTimestampToNow)
                } ?: false
        } ?: false
    }

    private fun start(
        schedule: ScheduleSchema,
        config: Map<String, Any>,
        setCollectionTimestampToNow: Boolean
    ): Boolean {
        runningObservations[schedule]?.observationConfig(config)
        return if (runningObservations[schedule]?.start(
                schedule.observationId,
                schedule.scheduleId.toHexString()
            ) == true
        ) {
            if (setCollectionTimestampToNow && schedule.getState() == ScheduleState.ACTIVE) {
                observationRepository.lastCollection(
                    schedule.observationType,
                    RealmInstant.now().epochSeconds
                )
            }
            setObservationState(schedule, ScheduleState.RUNNING)
            Napier.d { "Recording started of $schedule" }
            true
        } else {
            Napier.d { "Could not start $schedule!" }
            false
        }
    }

    fun pause(scheduleId: String) {
        scope.launch {
            findOrCreateObservation(scheduleId)?.let {
                Napier.d { "Pausing schedule: $it" }
                runningObservations[it]?.stop(it.observationId)
                setObservationState(it, ScheduleState.PAUSED)
                Napier.d { "Recording paused of ${it.scheduleId}" }
            }
        }
    }

    fun stop(scheduleId: String) {
        scope.launch {
            findOrCreateObservation(scheduleId)?.let {
                Napier.d { "Stopping schedule: $it" }
                runningObservations[it]?.stop(it.observationId)
                setObservationState(it, ScheduleState.DONE)
                runningObservations.remove(it)
                Napier.d { "Recording stopped of ${it.scheduleId}" }
            }
        }
    }

    fun stopAll() {
        Napier.d { "Stopping all observations..." }
        stopAllInList()
    }

    fun updateTaskStates() {
        scheduleRepository.updateTaskStates(observationFactory)
    }

    fun hasRunningTasks() = runningObservations.isNotEmpty()

    private fun stopAllInList() {
        Napier.d { "Running Observations to be stopped: $runningObservations" }
        runningObservations.forEach {
            stop(it.key.scheduleId.toHexString())
        }
    }

    private suspend fun findOrCreateObservation(scheduleId: String): ScheduleSchema? {
        return scheduleRepository.scheduleWithId(scheduleId).firstOrNull()?.let {
            if (findOrCreateObservation(it)) it else null
        }
    }

    private fun findOrCreateObservation(schedule: ScheduleSchema): Boolean {
        return (runningObservations[schedule] ?: observationFactory.observation(schedule.observationType)
                ?.let { observation ->
                    runningObservations[schedule] = observation
                    schedule
                }) != null
    }

    private fun setObservationState(schedule: ScheduleSchema, state: ScheduleState) {
        if (state != ScheduleState.DONE) {
            scheduleRepository.setRunningStateFor(schedule.scheduleId.toHexString(), state)
        } else {
            scheduleRepository.setCompletionStateFor(schedule.scheduleId.toHexString(), true)
            dataPointCountRepository.delete(schedule.scheduleId.toHexString())
        }
    }
}