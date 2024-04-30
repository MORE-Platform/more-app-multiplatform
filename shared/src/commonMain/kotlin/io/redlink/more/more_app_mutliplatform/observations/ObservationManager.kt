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
import io.realm.kotlin.ext.copyFromRealm
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlin.math.ceil

class ObservationManager(
    private val observationFactory: ObservationFactory,
    private val dataRecorder: DataRecorder
) {
    private val scheduleRepository = ScheduleRepository()
    private val dataPointCountRepository = DataPointCountRepository()
    private val observationRepository = ObservationRepository()

    private val runningObservations = mutableMapOf<String, Observation>()

    private val scheduleSchemaList = mutableSetOf<ScheduleSchema>()

    private val currentlyRunning = mutableSetOf<String>()
    private var upToDateTimestamps: Map<String, RealmInstant> = emptyMap()

    fun activateScheduleUpdate() {
        Napier.i(tag = "ObservationManager::activateScheduleUpdate") { "ObservationManager: ScheduleUpdater activating..." }
        StudyScope.launch {
            scheduleRepository.allSchedulesWithStatus(true).cancellable().collect { list ->
                if (runningObservations.isNotEmpty()) {
                    list.filter { it.scheduleId.toHexString() in runningObservations.keys }
                        .map { it.scheduleId.toHexString() }.forEach {
                            stop(it)
                            dataPointCountRepository.delete(it)
                        }
                }
            }
        }
        val firstCall = ceil(Clock.System.now().toEpochMilliseconds() / 60_000.0).toLong() * 60_000
        StudyScope.launch {
            delay(firstCall - Clock.System.now().toEpochMilliseconds())
            StudyScope.repeatedLaunch(30000L) {
                updateTaskStates()
            }
        }
        StudyScope.launch {
            observationRepository.collectAllTimestamps().cancellable().collect {
                upToDateTimestamps = it
            }
        }
    }

    suspend fun restartStillRunning(): Set<String> {
        val startedObservations = mutableSetOf<String>()
        scheduleRepository.allScheduleWithRunningState().cancellable().firstOrNull()?.let { list ->
            Napier.i(tag = "ObservationManager::restartStillRunning") { "Restarting schedules: $list" }
            list.filter { it.scheduleId.toHexString() !in runningObservations.keys }.forEach {
                if (start(it.scheduleId.toHexString())) {
                    startedObservations.add(it.scheduleId.toHexString())
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
                    observationRepository.getObservationByObservationId(scheduleSchema.observationId)
                        ?.let { observation ->
                            Napier.i(tag = "ObservationManager::start") { "Found Observation Config: ${observation.configAsMap()}" }
                            val config = observation.configAsMap().toMutableMap()
                            scheduleSchema.start?.let {
                                config[Observation.CONFIG_TASK_START] = it.epochSeconds
                            }
                            scheduleSchema.end?.let {
                                config[Observation.CONFIG_TASK_STOP] = it.epochSeconds
                            }
                            config[Observation.SCHEDULE_ID] =
                                scheduleSchema.scheduleId.toHexString()
                            if (scheduleSchema.getState() == ScheduleState.PAUSED) {
                                config[Observation.CONFIG_LAST_COLLECTION_TIMESTAMP] =
                                    observation.collectionTimestamp.epochSeconds
                            }
                            start(scheduleSchema, config)
                        } ?: false
                if (!result) {
                    Napier.w(tag = "ObservationManager::start") { "Could not retrieve Observation Schema for schedule: $scheduleSchema" }
                    runningObservations.remove(scheduleId)
                    scheduleSchemaList.removeAll { it.scheduleId.toHexString() == scheduleId }
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
        schedule: ScheduleSchema,
        config: Map<String, Any>
    ): Boolean {
        runningObservations[schedule.scheduleId.toHexString()]?.observationConfig(config)
        return if (runningObservations[schedule.scheduleId.toHexString()]?.start(
                schedule.observationId,
                schedule.scheduleId.toHexString()
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
            scheduleSchemaList.firstOrNull { it.scheduleId.toHexString() == scheduleId }?.let {
                Napier.i(tag = "ObservationManager::pause") { "Pausing schedule: $it" }
                observation.stop(scheduleId)
                setObservationState(it, ScheduleState.PAUSED)
                Napier.i(tag = "ObservationManager::pause") { "Recording paused of ${it.scheduleId}" }
            }
        } ?: kotlin.run {
            setObservationState(scheduleId, ScheduleState.PAUSED)
        }
        currentlyRunning.remove(scheduleId)
    }

    fun pauseObservationType(type: String) {
        Napier.i(tag = "ObservationManager::pauseObservationType") { "Pausing Observation type: $type" }
        runningObservations.filterValues { it.observationType.observationType == type }
            .forEach { (key, value) ->
                Napier.d(tag = "ObservationManager::pauseObservationType") { "Pausing schedule: $key" }
                value.stop(key)
                setObservationState(key, ScheduleState.PAUSED)
                currentlyRunning.remove(key)
                Napier.d(tag = "ObservationManager::pauseObservationType") { "Recording paused of $key" }
            }
    }

    fun startObservationType(type: String) {
        StudyScope.launch {
            Napier.d(tag = "ObservationManager::startObservationType") { "Restarting Observations with type: $type" }
            scheduleRepository.allSchedulesWithStatus(false)
                .firstOrNull()
                ?.filter { it.observationType == type && it.getState().active() }
                ?.forEach {
                    if (start(it.scheduleId.toHexString())) {
                        Napier.i(tag = "ObservationManager::startObservationType") { "Started Schedule: $it" }
                    } else {
                        currentlyRunning.remove(it.scheduleId.toHexString())
                        Napier.i(tag = "ObservationManager::startObservationType") { "Failed to start schedule: $it" }
                    }
                }
        }
    }

    fun stop(scheduleId: String) {
        runningObservations[scheduleId]?.let { observation ->
            scheduleSchemaList.firstOrNull { it.scheduleId.toHexString() == scheduleId }?.let {
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
        Napier.d(tag = "ObservationManager::updateTaskStates") { "" }
        scheduleRepository.updateTaskStates(observationFactory, dataRecorder)
    }

    fun hasRunningTasks() = currentlyRunning.isNotEmpty()

    fun allRunningObservations() = runningObservations.toMap()

    private fun stopAllInList() {
        Napier.d(tag = "ObservationManager::stopAllInList") { "Running Observations to be stopped: $runningObservations" }
        val runningObs = runningObservations.toList()
        runningObs.forEach { (scheduleId, observation) ->
            observation.stopAndFinish(scheduleId)
            scheduleRepository.setCompletionStateFor(scheduleId, true)
            dataPointCountRepository.delete(scheduleId)
            runningObservations.remove(scheduleId)
            scheduleSchemaList.removeAll { it.scheduleId.toHexString() == scheduleId }
            currentlyRunning.clear()
        }
    }

    fun collectAllData(onCompletion: (Boolean) -> Unit) {
        StudyScope.launch {
            restartStillRunning()
            if (!hasRunningTasks()) {
                onCompletion(true)
            }
            var counter = 0
            runningObservations.values.forEach {
                upToDateTimestamps[it.observationType.observationType]?.let { lastTimestamp ->
                    it.store(lastTimestamp.epochSeconds, Clock.System.now().epochSeconds) {
                        if (++counter == runningObservations.size) {
                            onCompletion(true)
                        }
                    }
                } ?: kotlin.run {
                    if (++counter == runningObservations.size) {
                        onCompletion(true)
                    }
                }
            }
        }
    }

    private suspend fun findOrCreateObservation(scheduleId: String): ScheduleSchema? {
        return scheduleRepository.scheduleWithId(scheduleId).firstOrNull()?.let {
            val fixedScheduleSchema = it.copyFromRealm()
            Napier.d(tag = "ObservationManager::findOrCreateObservation") { "Found Schema $it" }
            if (findOrCreateObservation(fixedScheduleSchema)) fixedScheduleSchema else null
        }
    }

    private fun findOrCreateObservation(schedule: ScheduleSchema): Boolean {
        return (runningObservations[schedule.scheduleId.toHexString()]
            ?: observationFactory.observation(schedule.observationType)
                ?.let { observation ->
                    Napier.d(tag = "ObservationManager::findOrCreateObservation") { "Found Observation for ScheduleSchema: $schedule : $observation" }
                    runningObservations[schedule.scheduleId.toHexString()] = observation
                    scheduleSchemaList.add(schedule)
                    schedule
                }) != null
    }

    private fun setObservationState(schedule: ScheduleSchema, state: ScheduleState) {
        Napier.i(tag = "ObservationManager::setObservationState") { "New Schedule State for Schema: $schedule; ${schedule.state} -> $state" }
        setObservationState(schedule.scheduleId.toHexString(), state)
    }

    private fun setObservationState(scheduleId: String, state: ScheduleState) {
        if (state != ScheduleState.DONE) {
            scheduleRepository.setRunningStateFor(scheduleId, state)
        } else {
            scheduleRepository.setCompletionStateFor(scheduleId, true)
            dataPointCountRepository.delete(scheduleId)
        }
    }
}