package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.asString

abstract class Observation(val observationTypeImpl: ObservationTypeImpl) {
    private var dataManager: ObservationDataManager? = null
    private var running = false
    private val observationIds = mutableSetOf<String>()
    private val scheduleIds = mutableMapOf<String, String>()
    private val config = mutableMapOf<String, Any>()
    private var configChanged = false

    fun start(observationId: String, scheduleId: String): Boolean {
        observationIds.add(observationId)
        scheduleIds[scheduleId] = observationId
        if (configChanged) {
            stopAndFinish()
            configChanged = false
        }
        return if (!running) {
            Napier.i { "Observation with type ${observationTypeImpl.observationType} starting" }
            applyObservationConfig(config)
            running = start()
            return running
        } else true
    }

    fun stop(observationId: String) {
        observationIds.remove(observationId)
        finish()
        if (observationIds.isEmpty()) {
            stop()
            running = false
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

    protected abstract fun stop()

    abstract fun observerAccessible(): Boolean

    protected abstract fun applyObservationConfig(settings: Map<String, Any>)

    fun storeData(data: Any) {
        val observationDataSchemas = observationIds.map { ObservationDataSchema().apply {
            this.observationId = it
            if (this.timestamp == null) {
                this.timestamp = RealmInstant.now()
            }
            this.dataValue = data.asString() ?: ""
        } }
        dataManager?.add(observationDataSchemas, scheduleIds.keys.toSet())
    }

    private fun stopAndFinish() {
        stop()
        finish()
    }

    private fun finish() {
        dataManager?.saveAndSend()
    }

    fun removeDataCount() {
        scheduleIds.keys.forEach {
            dataManager?.removeDataPointCount(it)
        }
        scheduleIds.clear()
    }


    fun isRunning() = running
}