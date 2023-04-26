package io.redlink.more.more_app_mutliplatform.observations


abstract class ObservationFactory(private val dataManager: ObservationDataManager) {
    val observations = mutableSetOf<Observation>()

    fun observationTypes() = observations.map { it.observationType.observationType }.toSet()

    fun sensorPermissions() =
        observations.map { it.observationType.sensorPermissions }.flatten().toSet()

    fun observationTypesNeedingRestartingAfterAppClosure() =
        observations.filter { it.needsToRestartAfterAppClosure() }
            .map { it.observationType.observationType }.toSet()

    fun observation(type: String): Observation? {
        return observations.firstOrNull { it.observationType.observationType == type }?.apply {
            if (!this.observationDataManagerAdded()) {
                setDataManager(dataManager)
            }
        }
    }
}