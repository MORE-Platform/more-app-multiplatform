package io.redlink.more.more_app_mutliplatform.observations

import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.asString

abstract class Observation(val observationTypeImpl: ObservationTypeImpl) {
    private var dataManager: ObservationDataManager? = null
    protected var running = false

    private var observationID: String? = null
    private var scheduleId: String? = null

    abstract fun start(observationId: String): Boolean

    abstract fun stop()

    abstract fun observerAccessible(): Boolean

    abstract fun setObservationConfig(settings: Map<String, Any>)

    fun setObservationId(id: String) {
        observationID = id
    }

    fun setScheduleId(id: String) {
        scheduleId = id
    }

    fun setDataManager(observationDataManager: ObservationDataManager) {
        dataManager = observationDataManager
    }

    fun storeData(data: Any, dateTime: Long = -1) {
        observationID?.let {
            dataManager?.add(observationTypeImpl.addObservationType(ObservationDataSchema().apply {
                this.observationId = it
                if (dateTime >= 0) {
                    this.timestamp = RealmInstant.from(dateTime, 0)

                } else if (this.timestamp == null) {
                    this.timestamp = RealmInstant.now()
                }
                this.dataValue = data.asString() ?: ""
            }))
        }
    }

    fun finish() {
        scheduleId?.let { scheduleId ->
            dataManager?.saveAndSend(scheduleId)
        }
    }

    fun isRunning() = running
}