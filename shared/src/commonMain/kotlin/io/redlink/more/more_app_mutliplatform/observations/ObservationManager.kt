package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.util.createUUID
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.ScheduleState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ObservationManager(private val observationFactory: ObservationFactory) {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val scheduleRepository = ScheduleRepository()
    private val observationRepository = ObservationRepository()

    val activeScheduleState: MutableStateFlow<Map<String, ScheduleState>> = MutableStateFlow(
        emptyMap()
    )

    private val runningObservations = mutableMapOf<String, Observation>()
    private val models = mutableSetOf<ObservationManagerModel>()

    fun stateForSchedule(scheduleId: String): ScheduleState {
        return activeScheduleState.replayCache.firstOrNull()?.let {
            it[scheduleId] ?: ScheduleState.NON
        } ?: ScheduleState.NON
    }

    fun start(scheduleId: String) {
        Napier.i { "Trying to start $scheduleId" }
        if (restart(scheduleId)) {
            return
        }
        val model = models.firstOrNull { it.scheduleId == scheduleId}
        if (model != null) {
            if (model.state != ScheduleState.RUNNING) {
                start(model.scheduleId, model.observationId, model.observationType)
            }
        } else {
            scope.launch {
                scheduleRepository.scheduleWithId(scheduleId).firstOrNull()?.let {
                    observationRepository.getObservationByObservationId(it.observationId)?.let { observation ->
                        start(scheduleId, observation.observationId, observation.observationType)
                    }
                }
            }
        }
    }

    private fun start(scheduleId: String, observationId: String, type: String) {
        val observationKey = runningObservations.entries.firstOrNull{it.value.observationTypeImpl.observationType == type}?.key
        if (observationKey != null) {
            start(scheduleId, observationId, type, observationKey)
        } else {
            observationFactory.observation(type)?.let {
                val uuid = createUUID()
                runningObservations[uuid] = it
                start(scheduleId, observationId, type, uuid)
            }
        }
    }

    private fun start(
        scheduleId: String,
        observationId: String,
        type: String,
        observationUUID: String
    ) {
        Napier.i { "Starting $scheduleId with type $type" }
        if (runningObservations[observationUUID]?.start(observationId, scheduleId) == true) {
            val model = models.firstOrNull { it.scheduleId == scheduleId}
            if (model != null) {
                if (model.observationUUID == model.observationUUID) {
                    model.state = ScheduleState.RUNNING
                } else {
                    models.remove(model)
                    models.add(ObservationManagerModel(
                        scheduleId,
                        observationId,
                        type,
                        observationUUID,
                        ScheduleState.RUNNING
                    ))
                }
            } else {
                models.add(ObservationManagerModel(
                    scheduleId,
                    observationId,
                    type,
                    observationUUID,
                    ScheduleState.RUNNING
                ))
            }
            setObservationState(scheduleId, ScheduleState.RUNNING)
            Napier.i { "Recording started of $scheduleId" }
        }
    }

    private fun restart(scheduleId: String): Boolean {
        return models.firstOrNull{it.scheduleId == scheduleId && it.state == ScheduleState.PAUSED}?.let {
            Napier.i { "Restarting schedule: $scheduleId" }
            if (runningObservations[it.observationUUID]?.start(it.observationId, it.scheduleId) == true) {
                it.state = ScheduleState.RUNNING
                setObservationState(scheduleId, ScheduleState.RUNNING)
                Napier.i { "Recording started of $scheduleId" }
                true
            } else false
        } ?: false
    }

    fun pause(scheduleId: String) {
        models.firstOrNull { it.scheduleId == scheduleId && it.state == ScheduleState.RUNNING }?.let {
            runningObservations[it.observationUUID]?.stop(it.observationId)
            it.state = ScheduleState.PAUSED
            setObservationState(scheduleId, ScheduleState.PAUSED)
        }
    }

    fun stop(scheduleId: String) {
        models.firstOrNull { it.scheduleId == scheduleId }?.let {
            runningObservations[it.observationUUID]?.stop(it.observationId)
            models.remove(it)
            setObservationState(scheduleId, ScheduleState.STOPPED)
        }
    }

    fun stopAll() {
        models.forEach {
            runningObservations[it.observationUUID]?.stop(it.observationId)
            setObservationState(it.scheduleId, ScheduleState.STOPPED)
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
}