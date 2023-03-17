package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
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

    fun start(scheduleId: String, observationId: String, type: String) {
        Napier.i { "Trying to start $scheduleId" }
        if (restart(scheduleId)) {
            return
        }
        val entry = runningObservations.entries.firstOrNull{it.value.observationTypeImpl.observationType == type}
        if (entry != null) {
            start(scheduleId, observationId, type, entry.value, entry.key)
        } else {
            observationFactory.observation(type)?.let {
                val uuid = createUUID()
                start(scheduleId, observationId, type, it, uuid)
                runningObservations[uuid] = it
            }
        }
    }

    private fun start(
        scheduleId: String,
        observationId: String,
        type: String,
        observation: Observation,
        observationUUID: String
    ) {
        if (observation.start(observationId)) {
            models.add(ObservationManagerModel(
                scheduleId,
                observationId,
                type,
                observationUUID,
                ScheduleState.RUNNING
            ))
            setObservationState(scheduleId, ScheduleState.RUNNING)
            Napier.i { "Recording started of $scheduleId" }
        }
    }

    private fun restart(scheduleId: String): Boolean {
        return models.firstOrNull{it.scheduleId == scheduleId && it.state == ScheduleState.PAUSED}?.let {
            if (runningObservations[it.observationUUID]?.start(it.observationId) == true) {
                it.state = ScheduleState.RUNNING
                setObservationState(scheduleId, ScheduleState.RUNNING)
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