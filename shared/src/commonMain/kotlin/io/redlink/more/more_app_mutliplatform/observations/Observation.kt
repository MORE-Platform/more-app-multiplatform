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

    fun start(observationId: String, scheduleId: String): Boolean {
        observationIds.add(observationId)
        scheduleIds[scheduleId] = observationId
        return if (!running) {
            Napier.i { "Observation with type ${observationTypeImpl.observationType} starting" }
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

    protected abstract fun start(): Boolean

    protected abstract fun stop()

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

    private fun finish() {
        dataManager?.saveAndSend()
    }

    fun removeDataCount() {
        scheduleIds.keys.forEach {
            dataManager?.removeDataPointCount(it)
        }
        scheduleIds.clear()
    }

    abstract fun observerAccessible(): Boolean

    fun isRunning() = running
}