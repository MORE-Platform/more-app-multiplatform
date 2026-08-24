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
package io.redlink.more.observations

import io.github.aakira.napier.Napier
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.models.ScheduleState
import io.redlink.more.scopes.AppDispatchers
import io.redlink.more.scopes.MoreDispatchers
import io.redlink.more.scopes.StudyMoreScope
import io.redlink.more.scopes.StudyScope
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlin.math.ceil

class ObservationManager(
    private val repositories: MainRepository,
    private val observationFactory: ObservationFactory,
    private val dataRecorder: DataRecorder,
    private val studyScope: StudyMoreScope = StudyScope,
    private val dispatchers: MoreDispatchers = AppDispatchers
) {

    private val runningObservations = mutableMapOf<String, Observation>()

    private val scheduleSchemaList = mutableSetOf<ScheduleEntity>()

    private val currentlyRunning = mutableSetOf<String>()
    var upToDateTimestamps: Map<String, Long> = emptyMap()

    fun activateScheduleUpdate() {
        Napier.i(tag = "ObservationManager::activateScheduleUpdate") { "ObservationManager: ScheduleUpdater activating..." }
        studyScope.launch(dispatchers.io) {
            repositories.schedule.allSchedulesWithStatus(true).distinctUntilChanged().cancellable()
                .collect { list ->
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
        val initialDelay = firstCall - Clock.System.now().toEpochMilliseconds()
        studyScope.repeatedLaunch(60000L, dispatchers.io, initialDelay) {
            updateTaskStates()
        }
        studyScope.launch(dispatchers.io) {
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
                return@let result
            } ?: false
            if (!result) {
                Napier.w(tag = "ObservationManager::start") { "Could not find observation for schema for scheduleId: $scheduleId" }
                currentlyRunning.remove(scheduleId)
            }
            result
        } else false

    }

    private suspend fun start(
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
                observation.stop(scheduleId, false)
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
                pause(key)
                Napier.d(tag = "ObservationManager::pauseObservationType") { "Recording paused of $key" }
            }
    }

    suspend fun startObservationType(type: String) {
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
        dataRecorder.startMultiple(
            repositories.schedule.allSchedulesWithStatus(false)
                .firstOrNull()
                ?.filter { it.observationType == type && it.getState().active() }
                ?.map { it.scheduleId }?.toSet() ?: emptySet()
        )
    }

    fun stop(scheduleId: String) {
        runningObservations[scheduleId]?.let { observation ->
            scheduleSchemaList.firstOrNull { it.scheduleId == scheduleId }?.let {
                Napier.i(tag = "ObservationManager::stop") { "Stopping schedule: $it" }
                observation.stop(scheduleId, false)
                observation.removeDataCount()
                setObservationState(it, ScheduleState.DONE)
                runningObservations.remove(scheduleId)
                scheduleSchemaList.remove(it)
                Napier.i(tag = "ObservationManager::stop") { "Observation removed: ${it.scheduleId}! Observations left: $runningObservations" }
                Napier.i(tag = "ObservationManager::stop") { "Recording stopped of ${it.scheduleId}" }
            }
        } ?: run {
            setObservationState(scheduleId, ScheduleState.DONE)
        }
        currentlyRunning.remove(scheduleId)
    }

    fun stopAll() {
        Napier.i(tag = "ObservationManager::stopAll") { "Stopping all observations..." }
        stopAllInList()
    }

    suspend fun updateTaskStates() {
        repositories.schedule.updateTaskStates(observationFactory, dataRecorder)
    }

    fun hasRunningTasks() = currentlyRunning.isNotEmpty()

    private fun stopAllInList() {
        Napier.d(tag = "ObservationManager::stopAllInList") { "Running Observations to be stopped: $runningObservations" }
        val runningObs = runningObservations.toList()
        runningObs.forEach { (scheduleId, observation) ->
            observation.stopAndFinish(scheduleId)
            studyScope.launch(dispatchers.io) {
                repositories.schedule.setCompletionStateFor(scheduleId, true)
            }
            repositories.dataPointCount.delete(scheduleId)
            runningObservations.remove(scheduleId)
            scheduleSchemaList.removeAll { it.scheduleId == scheduleId }
            currentlyRunning.clear()
        }
    }

    fun collectAllData(onCompletion: (Boolean) -> Unit) {
        studyScope.launch(dispatchers.io) {
            restartStillRunning()
            Napier.d(tag = "ObservationManager::collectAllData") { "Currently running observations: $runningObservations" }
            if (!hasRunningTasks()) {
                onCompletion(true)
                return@launch
            }
            var counter = 0
            val observationsToStore = runningObservations.values.toList()
            if (observationsToStore.isEmpty()) {
                onCompletion(true)
                return@launch
            }
            observationsToStore.forEach {
                upToDateTimestamps[it.observationType.observationType]?.let { lastTimestamp ->
                    it.store(lastTimestamp, Clock.System.now().epochSeconds) {
                        if (++counter == observationsToStore.size) {
                            onCompletion(true)
                        }
                    }
                } ?: run {
                    if (++counter == observationsToStore.size) {
                        onCompletion(true)
                    }
                }
            }
        }
    }

    private suspend fun findOrCreateObservation(scheduleId: String): ScheduleEntity? {
        return repositories.schedule.scheduleWithId(scheduleId).firstOrNull()?.let {
            Napier.d(tag = "ObservationManager::findOrCreateObservation") { "Found Schema $it" }
            if (findOrCreateObservation(it)) it else null
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
        studyScope.launch(dispatchers.io) {
            if (state != ScheduleState.DONE) {
                repositories.schedule.setRunningStateFor(scheduleId, state)
            } else {
                repositories.schedule.setCompletionStateFor(scheduleId, true)
                repositories.dataPointCount.delete(scheduleId)
            }
        }.second.invokeOnCompletion {
            Napier.d(tag = "ObservationManager::setObservationState") { "Schedule state updated for $scheduleId to $state" }
        }
    }
}