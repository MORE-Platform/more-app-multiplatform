package io.redlink.more.more_app_mutliplatform.observations

import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.asString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

abstract class Observation(val observationTypeImpl: ObservationTypeImpl) {
    private var dataManager: ObservationDataManager? = null
    protected var running = false

    private var observationID: String? = null

    fun setObservationId(id: String) {
        observationID = id
    }

    fun setDataManager(observationDataManager: ObservationDataManager) {
        dataManager = observationDataManager
    }

    abstract fun start(observationId: String): Boolean

    abstract fun stop()

    fun storeData(data: Any) {
        observationID?.let {
            dataManager?.add(observationTypeImpl.addObservationType(ObservationDataSchema().apply {
                this.observationId = it
                if (this.timestamp == null) {
                    this.timestamp = RealmInstant.now()
                }
                this.dataValue = data.asString() ?: ""
            }))
        }
    }

    fun finish() {
        dataManager?.saveAndSend()
    }

    abstract fun observerAccessible(): Boolean

    fun isRunning() = running
}