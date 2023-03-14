package io.redlink.more.more_app_mutliplatform.observations

import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow

abstract class ObservationFactory(networkService: NetworkService) {
    private val dataManager = ObservationDataManager(networkService)
    val observations = mutableSetOf<Observation>()

    fun observation(id: String, type: String, count: MutableStateFlow<DataPointCountSchema>): Observation? {
        return observations.firstOrNull { it.observationTypeImpl.observationType == type }?.apply {
            setObservationId(id)
            setDataManager(dataManager)
            setDataPointCount(count)
        }
    }

    fun sensorPermissions(): Set<String> {
        return observations.map { it.observationTypeImpl.sensorPermissions }.flatten().toSet()
    }
}