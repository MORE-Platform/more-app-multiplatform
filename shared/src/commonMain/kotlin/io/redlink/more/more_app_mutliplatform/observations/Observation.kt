package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.ObservationType

abstract class Observation(val observationType: ObservationType) {
    private val scheduleRepository = ScheduleRepository()
    private var dataManager: ObservationDataManager? = null
    private var running = false
    private val observationIds = mutableSetOf<String>()
    private val scheduleIds = mutableMapOf<String, String>()
    private val config = mutableMapOf<String, Any>()
    private var configChanged = false

    fun apply(observationId: String, scheduleId: String) {
        observationIds.add(observationId)
        scheduleIds[scheduleId] = observationId
    }

    fun remove(observationId: String, scheduleId: String) {
        observationIds.remove(observationId)
        scheduleIds.remove(scheduleId)
    }

    fun start(observationId: String, scheduleId: String): Boolean {
        observationIds.add(observationId)
        scheduleIds[scheduleId] = observationId
        if (running && configChanged) {
            stopAndFinish()
        }
        configChanged = false
        return if (!running) {
            Napier.i { "Observation with type ${observationType.observationType} starting" }
            applyObservationConfig(config)
            running = start()

            return running
        } else true
    }

    fun stop(observationId: String) {
        if (observationIds.size <= 1) {
            stop {
                finish()
                observationIds.remove(observationId)
                scheduleIds.filterValues { it == observationId }.keys.forEach { scheduleId ->
                    scheduleIds.remove(scheduleId)
                }
                if (observationIds.isEmpty()) {
                    running = false
                }
            }
        } else {
            finish()
        }
    }

    fun observationDataManagerAdded() = dataManager != null

    fun setDataManager(observationDataManager: ObservationDataManager) {
        dataManager = observationDataManager
    }

    fun observationConfig(settings: Map<String, Any>) {
        if (settings.isNotEmpty()) {
            this.config += settings
            configChanged = true
        }
    }

    protected abstract fun start(): Boolean

    protected abstract fun stop(onCompletion: () -> Unit)

    abstract fun observerAccessible(): Boolean

    protected abstract fun applyObservationConfig(settings: Map<String, Any>)

    open fun needsToRestartAfterAppClosure() = false

    fun storeData(data: Any, timestamp: Long = -1) {
        val dataSchemas = ObservationDataSchema.fromData(observationIds.toSet(), setOf(
            ObservationBulkModel(data, timestamp)
        )).map { observationType.addObservationType(it) }
        dataManager?.add(dataSchemas, scheduleIds.keys)
    }

    fun storeData(data: List<ObservationBulkModel>, onCompletion: () -> Unit) {
        val dataSchemas = ObservationDataSchema.fromData(observationIds.toSet(), data).map { observationType.addObservationType(it) }
        dataManager?.add(dataSchemas, scheduleIds.keys)
        onCompletion()
    }

    protected fun stopAndFinish() {
        stop {
            finish()
        }
    }

    protected fun stopAndSetState(state: ScheduleState = ScheduleState.ACTIVE) {
        stop {
            finish()
            scheduleIds.keys.forEach { scheduleRepository.setRunningStateFor(it, state) }
        }
    }

    protected fun setState(state: ScheduleState = ScheduleState.ACTIVE) {
        scheduleIds.keys.forEach { scheduleRepository.setRunningStateFor(it, state) }
    }

    open fun store(start: Long = -1, end: Long = -1, onCompletion: () -> Unit) {
        dataManager?.store()
        onCompletion()
    }

    protected fun finish() {
        dataManager?.saveAndSend()
    }

    fun removeDataCount() {
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
    }
}