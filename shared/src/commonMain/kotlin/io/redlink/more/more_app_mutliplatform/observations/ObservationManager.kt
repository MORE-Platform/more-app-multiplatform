package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.util.createUUID
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ObservationManager(private val observationFactory: ObservationFactory) {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val scheduleRepository = ScheduleRepository()
    private val observationRepository = ObservationRepository()

    val activeScheduleState: MutableStateFlow<Map<String, ScheduleState>> = MutableStateFlow(
        emptyMap()
    )

    private val runningObservations = mutableMapOf<String, Observation>()
    private val models = mutableSetOf<ObservationManagerModel>()

    init {
        updateTaskStates()
        scope.launch {
            scheduleRepository.allScheduleWithRunningState(ScheduleState.DONE).collect { list ->
                list.forEach { schema ->
                    models.filter { it.scheduleId == schema.scheduleId.toHexString()}.forEach {
                        stop(it.scheduleId)
                    }
                }
            }
        }
    }

    fun stateForSchedule(scheduleId: String): ScheduleState {
        return activeScheduleState.replayCache.firstOrNull()?.let {
            it[scheduleId] ?: ScheduleState.DEACTIVATED
        } ?: ScheduleState.DEACTIVATED
    }

    fun start(scheduleId: String, completionHandler: (Boolean) -> Unit) {
        Napier.i { "Trying to start $scheduleId" }
        if (restart(scheduleId)) {
            completionHandler(true)
            return
        }
        val model = models.firstOrNull { it.scheduleId == scheduleId }
        if (model != null) {
            if (model.state != ScheduleState.RUNNING) {
                completionHandler(
                    start(
                        model.scheduleId,
                        model.observationId,
                        model.observationType,
                        model.config
                    )
                )
            }
        } else {
            scope.launch {
                scheduleRepository.scheduleWithId(scheduleId).firstOrNull()?.let { scheduleSchema ->
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
                                Napier.i { "Start: ${it.epochSeconds}" }
                                config[Observation.CONFIG_TASK_START] = it.epochSeconds
                            }
                            scheduleSchema.end?.let {
                                Napier.i { "End: ${it.epochSeconds}" }
                                config[Observation.CONFIG_TASK_STOP] = it.epochSeconds
                            }
                            config[Observation.SCHEDULE_ID] =
                                scheduleSchema.scheduleId.toHexString()
                            Napier.i { config.toString() }
                            completionHandler(
                                start(
                                    scheduleId,
                                    observation.observationId,
                                    observation.observationType,
                                    config
                                )
                            )
                        }?: completionHandler(false)
                } ?: completionHandler(false)
            }
        }
    }

    private fun start(
        scheduleId: String,
        observationId: String,
        type: String,
        config: Map<String, Any>
    ): Boolean {
        val observationKey =
            runningObservations.entries.firstOrNull { it.value.observationType.observationType == type }?.key
        return if (observationKey != null) {
            start(scheduleId, observationId, type, observationKey, config)
        } else {
            observationFactory.observation(type)?.let {
                val uuid = createUUID()
                runningObservations[uuid] = it
                start(scheduleId, observationId, type, uuid, config)
            } ?: false
        }
    }

    private fun start(
        scheduleId: String,
        observationId: String,
        type: String,
        observationUUID: String,
        config: Map<String, Any>
    ): Boolean {
        Napier.i { "Starting $scheduleId with type $type" }
        runningObservations[observationUUID]?.observationConfig(config)
        return if (runningObservations[observationUUID]?.start(observationId, scheduleId) == true) {
            observationRepository.lastCollection(type, RealmInstant.now().epochSeconds)
            models.removeAll { it.scheduleId == scheduleId }
            models.add(
                ObservationManagerModel(
                    scheduleId,
                    observationId,
                    type,
                    observationUUID,
                    config,
                    ScheduleState.RUNNING
                )
            )
            setObservationState(scheduleId, ScheduleState.RUNNING)
            Napier.i { "Recording started of $scheduleId" }
            true
        } else false
    }

    private fun restart(scheduleId: String): Boolean {
        return models.firstOrNull { it.scheduleId == scheduleId && it.state == ScheduleState.PAUSED }
            ?.let {
                Napier.i { "Restarting schedule: $scheduleId" }
                if (runningObservations[it.observationUUID]?.start(
                        it.observationId,
                        it.scheduleId
                    ) == true
                ) {
                    it.state = ScheduleState.RUNNING
                    setObservationState(scheduleId, ScheduleState.RUNNING)
                    Napier.i { "Recording started of $scheduleId" }
                    true
                } else false
            } ?: false
    }

    fun pause(scheduleId: String) {
        models.firstOrNull { it.scheduleId == scheduleId && it.state == ScheduleState.RUNNING }
            ?.let {
                runningObservations[it.observationUUID]?.stop(it.observationId)
                it.state = ScheduleState.PAUSED
                setObservationState(scheduleId, ScheduleState.PAUSED)
            }
    }

    fun stop(scheduleId: String) {
        models.firstOrNull { it.scheduleId == scheduleId }?.let {
            runningObservations[it.observationUUID]?.stop(it.observationId)
            models.remove(it)
            runningObservations.remove(it.observationUUID)
            setObservationState(scheduleId, ScheduleState.DONE)
        }
    }

    fun stopAll() {
        models.forEach {
            runningObservations[it.observationUUID]?.stop(it.observationId)
            setObservationState(it.scheduleId, ScheduleState.DONE)
        }
    }

    fun updateTaskStates() {
        scheduleRepository.updateTaskStates(observationFactory)
    }

    private fun setObservationState(scheduleId: String, state: ScheduleState) {
        if (state != ScheduleState.DONE) {
            Napier.i { "Setting $scheduleId to state ${state.name}" }
            scheduleRepository.setRunningStateFor(scheduleId, state)
        } else {
            scheduleRepository.setCompletionStateFor(scheduleId, true)
        }
        scope.launch {
            activeScheduleState.firstOrNull()?.let {
                val mutable = it.toMutableMap()
                mutable[scheduleId] = state
                activeScheduleState.emit(mutable)
            }
        }
    }
}