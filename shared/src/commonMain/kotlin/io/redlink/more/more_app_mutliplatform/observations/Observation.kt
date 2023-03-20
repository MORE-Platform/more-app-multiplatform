package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.asString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

abstract class Observation(val observationTypeImpl: ObservationTypeImpl) {
    private var dataManager: ObservationDataManager? = null
    private var running = false
    private val observationIds = mutableSetOf<String>()

    fun start(observationId: String): Boolean {
        observationIds.add(observationId)
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
        dataManager?.add(observationDataSchemas)
    }

    fun finish() {
        dataManager?.saveAndSend()
    }

    abstract fun observerAccessible(): Boolean

    fun isRunning() = running
}