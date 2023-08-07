package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.ObservationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

abstract class Observation(val observationType: ObservationType) {
    private val scheduleRepository = ScheduleRepository()
    private var dataManager: ObservationDataManager? = null
    private var running = false
    private val observationIds = mutableSetOf<String>()
    private val scheduleIds = mutableMapOf<String, String>()
    private val config = mutableMapOf<String, Any>()
    private var configChanged = false

    protected var lastCollectionTimestamp: Instant = Clock.System.now()


    fun apply(observationId: String, scheduleId: String) {
        Napier.i(tag = "Observation::apply") { "Applying observation $observationId of type ${observationType.observationType} for schedule $scheduleId." }
        observationIds.add(observationId)
        scheduleIds[scheduleId] = observationId
    }

    fun remove(observationId: String, scheduleId: String) {
        Napier.i(tag = "Observation::remove") { "Removing observation $observationId of type ${observationType.observationType} for schedule $scheduleId." }
        observationIds.remove(observationId)
        scheduleIds.remove(scheduleId)
    }

    fun start(observationId: String, scheduleId: String): Boolean {
        observationIds.add(observationId)
        scheduleIds[scheduleId] = observationId
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

    fun stop(scheduleId: String) {
        Napier.i(tag = "Observation::stop") { "Stopping observation of type ${observationType.observationType} for schedule $scheduleId." }
        if (observationIds.size <= 1) {
            stop {
                finish()
                observationShutdown(scheduleId)
            }
        } else {
            finish()
        }
    }

    fun observationDataManagerAdded() = dataManager != null

    fun setDataManager(observationDataManager: ObservationDataManager) {
        Napier.i(tag = "Observation::setDataManager") { "Setting data manager for observation of type ${observationType.observationType}." }
        dataManager = observationDataManager
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
        this.lastCollectionTimestamp = Clock.System.now()
    }

    protected abstract fun start(): Boolean

    protected abstract fun stop(onCompletion: () -> Unit)

    abstract fun observerAccessible(): Boolean

    protected abstract fun applyObservationConfig(settings: Map<String, Any>)

    open fun bleDevicesNeeded(): Set<String> = emptySet()

    open fun ableToAutomaticallyStart() = true

    fun storeData(data: Any, timestamp: Long = -1, onCompletion: () -> Unit = {}) {
        val dataSchemas = ObservationDataSchema.fromData(observationIds.toSet(), setOf(
            ObservationBulkModel(data, timestamp)
        )).map { observationType.addObservationType(it) }
        Napier.i(tag = "Observation::storeData") { "Observation, with ids $observationIds, ${observationType.observationType} recorded a new data point!" }
        dataManager?.add(dataSchemas, scheduleIds.keys)
        onCompletion()
    }

    fun storeData(data: List<ObservationBulkModel>, onCompletion: () -> Unit) {
        val dataSchemas = ObservationDataSchema.fromData(observationIds.toSet(), data).map { observationType.addObservationType(it) }
        Napier.i(tag = "Observation::storeData") { "Observation, with ids $observationIds, ${observationType.observationType} recorded new datapoints!" }
        dataManager?.add(dataSchemas, scheduleIds.keys)
        onCompletion()
    }

    fun stopAndFinish(scheduleId: String) {
        Napier.i(tag = "Observation::stopAndFinish") { "Stopping and finishing observation ${observationType.observationType} for observationIds: $observationIds" }
        stop {
            finish()
            observationShutdown(scheduleId)
        }
    }

    fun stopAndSetState(state: ScheduleState = ScheduleState.ACTIVE, scheduleId: String?) {
        Napier.d(tag = "Observation::stopAndSetState") { "Stopping observation of type ${observationType.observationType} and setting state to $state for schedule $scheduleId." }
        stop {
            finish()
            scheduleIds.keys.forEach { scheduleRepository.setRunningStateFor(it, state) }
            scheduleId?.let {
                observationShutdown(it)
            }
        }
    }

    fun stopAndSetDone(scheduleId: String) {
        Napier.d(tag = "Observation::stopAndSetDone") { "Stopping observation of type ${observationType.observationType} and setting done for schedule $scheduleId." }
        stop {
            finish()
            scheduleIds.keys.forEach { scheduleRepository.setCompletionStateFor(it, true) }
            observationShutdown(scheduleId)
            removeDataCount()
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

    protected fun finish() {
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
    }
}
