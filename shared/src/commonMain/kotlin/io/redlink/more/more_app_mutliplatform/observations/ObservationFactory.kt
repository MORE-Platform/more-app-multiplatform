package io.redlink.more.more_app_mutliplatform.observations

abstract class ObservationFactory(private val dataManager: ObservationDataManager) {
    val observations = mutableSetOf<Observation>()

    fun observation(type: String): Observation? {
        return observations.firstOrNull { it.observationType.observationType == type }?.apply {
            if (!this.observationDataManagerAdded()) {
                setDataManager(dataManager)
            }
        }
    }

    fun sensorPermissions(): Set<String> {
        return observations.map { it.observationType.sensorPermissions }.flatten().toSet()
    }
}