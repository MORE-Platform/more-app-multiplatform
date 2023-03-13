package io.redlink.more.more_app_mutliplatform.observations

import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.asString

abstract class Observation(val observationTypeImpl: ObservationTypeImpl) {
    private var dataManager: ObservationDataManager? = null
    protected var running = false

    private var observationID: String? = null

    abstract fun start(observationId: String): Boolean

    abstract fun stop()

    abstract fun observerAccessible(): Boolean

    fun setObservationId(id: String) {
        observationID = id
    }

    fun setDataManager(observationDataManager: ObservationDataManager) {
        dataManager = observationDataManager
    }

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

    fun isRunning() = running
}