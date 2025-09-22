/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.entities.ScheduleEntity
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.scopes.StudyScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlin.math.ceil

class ObservationManager(
    private val repositories: MainRepository,
    private val observationFactory: ObservationFactory,
    private val dataRecorder: DataRecorder
) {

    private val runningObservations = mutableMapOf<String, Observation>()

    private val scheduleSchemaList = mutableSetOf<ScheduleEntity>()

    private val currentlyRunning = mutableSetOf<String>()
    private var upToDateTimestamps: Map<String, Long> = emptyMap()

    fun activateScheduleUpdate() {
        Napier.i(tag = "ObservationManager::activateScheduleUpdate") { "ObservationManager: ScheduleUpdater activating..." }
        StudyScope.launch(Dispatchers.IO) {
            repositories.schedule.allSchedulesWithStatus(true).cancellable().collect { list ->
                if (runningObservations.isNotEmpty()) {
                    list.filter { it.scheduleId in runningObservations.keys }
                        .map { it.scheduleId }.forEach {
                            stop(it)
                            repositories.dataPointCount.delete(it)
                        }
                }
            }
        }
        val firstCall = ceil(Clock.System.now().toEpochMilliseconds() / 60_000.0).toLong() * 60_000
        StudyScope.launch {
            delay(firstCall - Clock.System.now().toEpochMilliseconds())
            StudyScope.repeatedLaunch(30000L, Dispatchers.IO) {
                updateTaskStates()
            }
        }
        StudyScope.launch(Dispatchers.IO) {
            repositories.observation.collectAllTimestamps().cancellable().collect {
                upToDateTimestamps = it
            }
        }
    }

    suspend fun restartStillRunning(): Set<String> {
        val startedObservations = mutableSetOf<String>()
        repositories.schedule.allScheduleWithRunningState().cancellable().firstOrNull()
            ?.let { list ->
                Napier.i(tag = "ObservationManager::restartStillRunning") { "Restarting schedules: $list" }
                list.filter { it.scheduleId !in runningObservations.keys }.forEach {
                    if (start(it.scheduleId)) {
                        startedObservations.add(it.scheduleId)
                    }
                }
            }
        return startedObservations
    }

    suspend fun start(scheduleId: String): Boolean {
        return if (scheduleId !in currentlyRunning) {
            currentlyRunning.add(scheduleId)
            Napier.i(tag = "ObservationManager::start") { "Trying to start $scheduleId..." }
            val result = findOrCreateObservation(scheduleId)?.let { scheduleSchema ->
                Napier.i(tag = "ObservationManager::start") { "Trying to start schedule: $scheduleSchema" }
                val result =
                    repositories.observation.getObservationByObservationId(scheduleSchema.observationId)
                        ?.let { observation ->
                            Napier.i(tag = "ObservationManager::start") { "Found Observation Config: ${observation.configAsMap()}" }
                            val config = observation.configAsMap().toMutableMap()
                            scheduleSchema.start?.let {
                                config[Observation.CONFIG_TASK_START] = it
                            }
                            scheduleSchema.end?.let {
                                config[Observation.CONFIG_TASK_STOP] = it
                            }
                            config[Observation.SCHEDULE_ID] =
                                scheduleSchema.scheduleId
                            if (scheduleSchema.getState() == ScheduleState.PAUSED) {
                                config[Observation.CONFIG_LAST_COLLECTION_TIMESTAMP] =
                                    observation.collectionTimestamp
                            }
                            start(scheduleSchema, config)
                        } ?: false
                if (!result) {
                    Napier.w(tag = "ObservationManager::start") { "Could not retrieve Observation Schema for schedule: $scheduleSchema" }
                    runningObservations.remove(scheduleId)
                    scheduleSchemaList.removeAll { it.scheduleId == scheduleId }
                    currentlyRunning.remove(scheduleId)
                }
                result
            } ?: false
            if (!result) {
                Napier.w(tag = "ObservationManager::start") { "Could not find observation for schema for scheduleId: $scheduleId" }
                currentlyRunning.remove(scheduleId)
            }
            result
        } else false

    }

    private fun start(
        schedule: ScheduleEntity,
        config: Map<String, Any>
    ): Boolean {
        runningObservations[schedule.scheduleId]?.observationConfig(config)
        return if (runningObservations[schedule.scheduleId]?.start(
                schedule.observationId,
                schedule.scheduleId
            ) == true
        ) {
            setObservationState(schedule, ScheduleState.RUNNING)
            Napier.i(tag = "ObservationManager::start") { "Recording started of $schedule" }
            true
        } else {
            Napier.i(tag = "ObservationManager::start") { "Could not start $schedule!" }
            false
        }
    }

    fun pause(scheduleId: String) {
        runningObservations[scheduleId]?.let { observation ->
            scheduleSchemaList.firstOrNull { it.scheduleId == scheduleId }?.let {
                Napier.i(tag = "ObservationManager::pause") { "Pausing schedule: $it" }
                observation.stop(scheduleId)
                setObservationState(it, ScheduleState.PAUSED)
                Napier.i(tag = "ObservationManager::pause") { "Recording paused of ${it.scheduleId}" }
            }
        } ?: run {
            setObservationState(scheduleId, ScheduleState.PAUSED)
        }
        currentlyRunning.remove(scheduleId)
    }

    fun pauseObservationType(type: String) {
        Napier.i(tag = "ObservationManager::pauseObservationType") { "Pausing Observation type: $type" }
        runningObservations.filterValues { it.observationType.observationType == type }
            .keys.forEach { key ->
                Napier.d(tag = "ObservationManager::pauseObservationType") { "Pausing schedule: $key" }
                dataRecorder.pause(key)
                Napier.d(tag = "ObservationManager::pauseObservationType") { "Recording paused of $key" }
            }
    }

    fun startObservationType(type: String) {
        StudyScope.launch(Dispatchers.IO) {
            Napier.d(tag = "ObservationManager::startObservationType") { "Restarting Observations with type: $type" }
            repositories.schedule.allSchedulesWithStatus(false)
                .firstOrNull()
                ?.filter { it.observationType == type && it.getState().active() }
                ?.forEach {
                    if (start(it.scheduleId)) {
                        Napier.i(tag = "ObservationManager::startObservationType") { "Started Schedule: $it" }
                    } else {
                        currentlyRunning.remove(it.scheduleId)
                        Napier.i(tag = "ObservationManager::startObservationType") { "Failed to start schedule: $it" }
                    }
                }
        }
    }

    fun stop(scheduleId: String) {
        runningObservations[scheduleId]?.let { observation ->
            scheduleSchemaList.firstOrNull { it.scheduleId == scheduleId }?.let {
                Napier.i(tag = "ObservationManager::stop") { "Stopping schedule: $it" }
                observation.stop(scheduleId)
                observation.removeDataCount()
                setObservationState(it, ScheduleState.DONE)
                runningObservations.remove(scheduleId)
                scheduleSchemaList.remove(it)
                Napier.i(tag = "ObservationManager::stop") { "Observation removed: ${it.scheduleId}! Observations left: $runningObservations" }
                Napier.i(tag = "ObservationManager::stop") { "Recording stopped of ${it.scheduleId}" }
            }
        } ?: kotlin.run {
            setObservationState(scheduleId, ScheduleState.DONE)
        }
        currentlyRunning.remove(scheduleId)
    }

    fun stopAll() {
        Napier.i(tag = "ObservationManager::stopAll") { "Stopping all observations..." }
        stopAllInList()
    }

    fun updateTaskStates() {
        repositories.schedule.updateTaskStates(observationFactory, dataRecorder)
    }

    suspend fun updateTaskStatesWithBLEDevices() {
        repositories.schedule.updateTaskStatesWithBLEDevices(observationFactory, dataRecorder)
    }

    fun hasRunningTasks() = currentlyRunning.isNotEmpty()

    fun allRunningObservations() = runningObservations.toMap()

    private fun stopAllInList() {
        Napier.d(tag = "ObservationManager::stopAllInList") { "Running Observations to be stopped: $runningObservations" }
        val runningObs = runningObservations.toList()
        runningObs.forEach { (scheduleId, observation) ->
            observation.stopAndFinish(scheduleId)
            StudyScope.launch(Dispatchers.IO) {
                repositories.schedule.setCompletionStateFor(scheduleId, true)
            }
            repositories.dataPointCount.delete(scheduleId)
            runningObservations.remove(scheduleId)
            scheduleSchemaList.removeAll { it.scheduleId == scheduleId }
            currentlyRunning.clear()
        }
    }

    fun collectAllData(onCompletion: (Boolean) -> Unit) {
        StudyScope.launch(Dispatchers.IO) {
            restartStillRunning()
            if (!hasRunningTasks()) {
                onCompletion(true)
            }
            var counter = 0
            runningObservations.values.forEach {
                upToDateTimestamps[it.observationType.observationType]?.let { lastTimestamp ->
                    it.store(lastTimestamp, Clock.System.now().epochSeconds) {
                        if (++counter == runningObservations.size) {
                            onCompletion(true)
                        }
                    }
                } ?: run {
                    if (++counter == runningObservations.size) {
                        onCompletion(true)
                    }
                }
            }
        }
    }

    private suspend fun findOrCreateObservation(scheduleId: String): ScheduleEntity? {
        return repositories.schedule.scheduleWithId(scheduleId).firstOrNull()?.let {
            val fixedScheduleSchema = it
            Napier.d(tag = "ObservationManager::findOrCreateObservation") { "Found Schema $it" }
            if (findOrCreateObservation(fixedScheduleSchema)) fixedScheduleSchema else null
        }
    }

    private fun findOrCreateObservation(schedule: ScheduleEntity): Boolean {
        return (runningObservations[schedule.scheduleId]
            ?: observationFactory.observation(schedule.observationType)
                ?.let { observation ->
                    Napier.d(tag = "ObservationManager::findOrCreateObservation") { "Found Observation for ScheduleSchema: $schedule : $observation" }
                    runningObservations[schedule.scheduleId] = observation
                    scheduleSchemaList.add(schedule)
                    schedule
                }) != null
    }

    private fun setObservationState(schedule: ScheduleEntity, state: ScheduleState) {
        Napier.i(tag = "ObservationManager::setObservationState") { "New Schedule State for Schema: $schedule; ${schedule.state} -> $state" }
        setObservationState(schedule.scheduleId, state)
    }

    private fun setObservationState(scheduleId: String, state: ScheduleState) {
        StudyScope.launch(Dispatchers.IO) {
            if (state != ScheduleState.DONE) {
                repositories.schedule.setRunningStateFor(scheduleId, state)
            } else {
                repositories.schedule.setCompletionStateFor(scheduleId, true)
                repositories.dataPointCount.delete(scheduleId)
            }
        }
    }
}