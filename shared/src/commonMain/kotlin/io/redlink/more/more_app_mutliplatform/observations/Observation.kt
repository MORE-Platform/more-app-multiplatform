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
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.ObservationType
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationManager
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

abstract class Observation(val observationType: ObservationType) {
    private val scheduleRepository = ScheduleRepository()
    private var dataManager: ObservationDataManager? = null
    private var notificationManager: NotificationManager? = null
    private val observationRepository = ObservationRepository()

    private var running = false
    private val observationIds = mutableSetOf<String>()
    private val scheduleIds = mutableMapOf<String, String>()
    private val notificationIds = mutableMapOf<String, String>()
    private val config = mutableMapOf<String, Any>()
    private var configChanged = false

    protected var lastCollectionTimestamp: Instant = Clock.System.now()

    var timestampCollectionJob: Job? = null

    private val _observationErrors = MutableStateFlow<Pair<String, Set<String>>>(
        Pair(
            this.observationType.observationType,
            emptySet()
        )
    )
    val observationErrors: StateFlow<Pair<String, Set<String>>> = _observationErrors;

    fun start(observationId: String, scheduleId: String, notificationId: String? = null): Boolean {
        observationIds.add(observationId)
        timestampCollectionJob?.cancel()
        timestampCollectionJob = StudyScope.launch {
            observationRepository.collectTimestampForObservationIds(observationIds).collect {
                lastCollectionTimestamp = Instant.fromEpochSeconds(it.epochSeconds)
                Napier.d(tag = "Observation::start") { "Last collection $lastCollectionTimestamp" }
            }
        }.second
        timestampCollectionJob?.invokeOnCompletion {
            timestampCollectionJob = null
        }
        scheduleIds[scheduleId] = observationId
        notificationId?.let {
            notificationIds[scheduleId] = notificationId
        }
        if (running && configChanged) {
            stopAndFinish(scheduleId)
        }
        configChanged = false
        return if (!running) {
            Napier.i(tag = "Observation::start") { "Observation with type ${observationType.observationType} starting" }
            applyObservationConfig(config)
            running = start()
            return running
        } else true
    }

    fun stop(scheduleId: String, removeNotification: Boolean = false) {
        Napier.i(tag = "Observation::stop") { "Stopping observation of type ${observationType.observationType} for schedule $scheduleId." }
        if (observationIds.size <= 1) {
            stop {
                timestampCollectionJob?.cancel()
                saveAndSend()
                observationShutdown(scheduleId)
            }
        } else {
            saveAndSend()
        }
        if (removeNotification) {
            handleNotification(scheduleId)
        }
        updateObservationErrors()
    }

    fun observationDataManagerAdded() = dataManager != null

    fun setDataManager(observationDataManager: ObservationDataManager) {
        Napier.i(tag = "Observation::setDataManager") { "Setting data manager for observation of type ${observationType.observationType}." }
        dataManager = observationDataManager
    }

    fun setNotificationManager(notificationManager: NotificationManager) {
        this.notificationManager = notificationManager
    }

    fun addNotificationId(scheduleId: String, notificationId: String) {
        notificationIds[scheduleId] = notificationId
    }

    fun observationConfig(settings: Map<String, Any>) {
        this.lastCollectionTimestamp = (settings[CONFIG_LAST_COLLECTION_TIMESTAMP] as? Long)?.let {
            Instant.fromEpochSeconds(it, 0)
        } ?: Clock.System.now()
        if (settings.isNotEmpty()) {
            Napier.i(tag = "Observation::observationConfig") { "Applying new observation settings for ${observationType.observationType}: $settings" }
            val newConfig = this.config + settings
            if (newConfig != this.config) {
                configChanged = true
                this.config += newConfig
            }
        }
    }

    protected fun collectionTimestampToNow() {
        Napier.d(tag = "Observation::collectionTimeStampToNow") { "Collecting timestamp" }
        lastCollectionTimestamp = Clock.System.now()
        observationRepository.lastCollection(
            observationIds.toSet(),
            lastCollectionTimestamp.epochSeconds
        )
    }

    protected abstract fun start(): Boolean

    protected abstract fun stop(onCompletion: () -> Unit)

    fun observerAccessible(): Boolean {
        val errors = observerErrors()
        Napier.d(tag = "Observation::observerAccessible") { errors.toString() }
        this._observationErrors.update { Pair(observationType.observationType, errors) }
        return errors.isEmpty()
    }

    protected open fun observerErrors(): Set<String> = emptySet()

    fun updateObservationErrors() {
        StudyScope.launch(Dispatchers.IO) {
            Napier.d(tag = "Observation::updateObservationErrors") { "ObservationErrors for ${observationType.observationType}" }
            _observationErrors.update { Pair(observationType.observationType, observerErrors()) }
        }
    }

    protected abstract fun applyObservationConfig(settings: Map<String, Any>)

    open fun bleDevicesNeeded(): Set<String> = emptySet()

    open fun ableToAutomaticallyStart() = true

    fun storeData(data: Any, timestamp: Long = -1, onCompletion: () -> Unit = {}) {
        val dataSchemas = ObservationDataSchema.fromData(
            observationIds.toSet(), setOf(ObservationBulkModel(data, timestamp))
        ).map { observationType.addObservationType(it) }
        Napier.i(tag = "Observation::storeData") { "Observation, with ids $observationIds, ${observationType.observationType} recorded a new data point!" }
        dataManager?.add(dataSchemas, scheduleIds.keys)
        onCompletion()
    }

    fun storeData(data: List<ObservationBulkModel>, onCompletion: () -> Unit) {
        val dataSchemas = ObservationDataSchema.fromData(observationIds.toSet(), data)
            .map { observationType.addObservationType(it) }
        Napier.i(tag = "Observation::storeData") { "Observation, with ids $observationIds, ${observationType.observationType} recorded new datapoints!" }
        dataManager?.add(dataSchemas, scheduleIds.keys)
        onCompletion()
    }

    fun stopAndFinish(scheduleId: String) {
        Napier.i(tag = "Observation::stopAndFinish") { "Stopping and finishing observation ${observationType.observationType} for observationIds: $observationIds" }
        stop {
            timestampCollectionJob?.cancel()
            saveAndSend()
            observationShutdown(scheduleId)
        }
        updateObservationErrors()
    }

    fun stopAndSetState(state: ScheduleState = ScheduleState.ACTIVE, scheduleId: String?) {
        Napier.d(tag = "Observation::stopAndSetState") { "Stopping observation of type ${observationType.observationType} and setting state to $state for schedule $scheduleId." }
        stop {
            timestampCollectionJob?.cancel()
            saveAndSend()
            scheduleIds.keys.forEach { scheduleRepository.setRunningStateFor(it, state) }
            scheduleId?.let {
                observationShutdown(it)
            }
        }
        updateObservationErrors()
    }

    fun stopAndSetDone(scheduleId: String) {
        Napier.d(tag = "Observation::stopAndSetDone") { "Stopping observation of type ${observationType.observationType} and setting done for schedule $scheduleId." }
        stop {
            timestampCollectionJob?.cancel()
            saveAndSend()
            scheduleIds.keys.forEach { scheduleRepository.setCompletionStateFor(it, true) }
            observationShutdown(scheduleId)
            removeDataCount()
            handleNotification(scheduleId)
            updateObservationErrors()
        }
    }

    open fun store(start: Long = -1, end: Long = -1, onCompletion: () -> Unit) {
        Napier.d(tag = "Observation::store") { "Storing data for observation of type ${observationType.observationType} with start time: $start, end time: $end." }
        dataManager?.store()
        onCompletion()
    }

    private fun observationShutdown(scheduleId: String) {
        val observationId = scheduleIds.remove(scheduleId)
        observationId?.let { observationIds.remove(it) }
        if (observationIds.isEmpty()) {
            config.clear()
            configChanged = false
            running = false
        }
    }

    private fun handleNotification(scheduleId: String) {
        notificationIds.remove(scheduleId)?.let {
            notificationManager?.markNotificationAsRead(it)
        }
    }

    protected fun showNotification(title: String, notificationBody: String) {
        val notification = NotificationSchema.build(title, notificationBody)
        Napier.d(tag = "Observation::showNotification") { "Showing notification: $notification" }
        notificationManager?.storeAndDisplayNotification(notification, true)
    }

    protected fun showObservationErrorNotification(
        notificationBody: String,
        fallbackTitle: String = "Error"
    ) {
        val schedulesSchemaFlows = scheduleIds.keys.map {
            scheduleRepository.scheduleWithId(it)
        }
        val combinedFlow = combine(schedulesSchemaFlows) { values ->
            values.mapNotNull { it }
        }

        StudyScope.launch {
            val scheduleSchemas = combinedFlow.first()
            val title =
                if (scheduleSchemas.isNotEmpty()) scheduleSchemas.map { it.observationTitle }
                    .joinToString(", ", limit = 5) else fallbackTitle
            withContext(Dispatchers.Main) {
                showNotification(title, notificationBody)
            }
        }
    }

    protected fun saveAndSend() {
        Napier.d(tag = "Observation::finish") { "Saving and sending data for observation of type ${observationType.observationType}." }
        dataManager?.saveAndSend()
    }

    fun removeDataCount() {
        Napier.d(tag = "Observation::removeDataCount") { "Removing data point count for observation of type ${observationType.observationType}." }
        scheduleIds.keys.forEach {
            dataManager?.removeDataPointCount(it)
        }
        scheduleIds.clear()
    }

    fun isRunning() = running

    companion object {
        const val CONFIG_TASK_START = "observation_start_date_time"
        const val CONFIG_TASK_STOP = "observation_stop_date_time"
        const val SCHEDULE_ID = "schedule_id"
        const val CONFIG_LAST_COLLECTION_TIMESTAMP = "observation_last_collection_timestamp"

        const val ERROR_DEVICE_NOT_CONNECTED = "error_device_not_connected"
    }
}
