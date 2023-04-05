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
        updateTaskStates()
        scope.launch {
            scheduleRepository.allScheduleWithRunningState(ScheduleState.DONE).collect { list ->
                list.forEach {
                    if (findOrCreateObservation(it)) {
                        runningObservations[it]?.stop(it.observationId)
                    }
                    dataPointCountRepository.delete(it.scheduleId.toHexString())
                }
            }
        }
    }

    fun start(scheduleId: String, completionHandler: (Boolean) -> Unit) {
        Napier.i { "Trying to start $scheduleId" }
        scope.launch {
            findOrCreateObservation(scheduleId)?.let { scheduleSchema ->
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
                        completionHandler(start(scheduleSchema, config))
                    } ?: completionHandler(false)
            } ?: completionHandler(false)
        }

    }

    private fun start(
        schedule: ScheduleSchema,
        config: Map<String, Any>
    ): Boolean {
        runningObservations[schedule]?.observationConfig(config)
        return if (runningObservations[schedule]?.start(
                schedule.observationId,
                schedule.scheduleId.toHexString()
            ) == true
        ) {
            observationRepository.lastCollection(
                schedule.observationType,
                RealmInstant.now().epochSeconds
            )
            setObservationState(schedule, ScheduleState.RUNNING)
            Napier.i { "Recording started of ${schedule.scheduleId.toHexString()}" }
            true
        } else false
    }

    fun pause(scheduleId: String) {
        scope.launch {
            findOrCreateObservation(scheduleId)?.let {
                runningObservations[it]?.stop(it.observationId)
                setObservationState(it, ScheduleState.PAUSED)
                Napier.i { "Recording paused of ${it.scheduleId}" }
            }
        }
    }

    fun stop(scheduleId: String) {
        scope.launch {
            findOrCreateObservation(scheduleId)?.let {
                runningObservations[it]?.stop(it.observationId)
                setObservationState(it, ScheduleState.DONE)
                runningObservations.remove(it)
                Napier.i { "Recording stopped of ${it.scheduleId}" }
            }
        }
    }

    fun stopAll() {
        stopAllInList()
        scope.launch {
            scheduleRepository.allScheduleWithRunningState().firstOrNull()?.let { list ->
                list.forEach { findOrCreateObservation(it) }
                stopAllInList()
            }
        }
    }

    fun updateTaskStates() {
        scheduleRepository.updateTaskStates(observationFactory)
    }

    private fun stopAllInList() {
        runningObservations.forEach {
            it.value.stop(it.key.observationId)
            setObservationState(it.key, ScheduleState.DONE)
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