package io.redlink.more.more_app_mutliplatform.observations

import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.extensions.asString
import kotlinx.coroutines.flow.MutableStateFlow

abstract class Observation(val observationTypeImpl: ObservationTypeImpl) {
    private var dataManager: ObservationDataManager? = null
    private var dataPointCountRepository: DataPointCountRepository = DataPointCountRepository()
    private var dataPointCount: MutableStateFlow<DataPointCountSchema> = MutableStateFlow(DataPointCountSchema())
    protected var running = false

    private var observationID: String? = null

    fun setObservationId(id: String) {
        observationID = id
        dataPointCount.value.scheduleId = id
    }

    fun setDataManager(observationDataManager: ObservationDataManager) {
        dataManager = observationDataManager
    }

    fun setDataPointCount(count: DataPointCountSchema) {
        dataPointCount = MutableStateFlow(count)
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
            dataPointCountRepository.upsert(dataPointCount.value.apply { count += 1 })
        }
    }

    fun finish() {
        dataManager?.saveAndSend()
        dataPointCountRepository.delete(dataPointCount.value)
    }

    abstract fun observerAccessible(): Boolean

    fun isRunning() = running
}