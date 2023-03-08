package io.redlink.more.more_app_mutliplatform.observations

import io.redlink.more.more_app_mutliplatform.services.network.NetworkService

abstract class ObservationFactory(networkService: NetworkService) {
    private val dataManager = ObservationDataManager(networkService)
    abstract val observations: Set<Observation>

    fun observation(id: String, type: String): Observation? {
        return observations.firstOrNull { it.observationTypeImpl.observationType == type }?.apply {
            setObservationId(id)
            setDataManager(dataManager)
        }
    }

    fun sensorPermissions(): Set<String> {
        return observations.map { it.observationTypeImpl.sensorPermissions }.flatten().toSet()
    }
}