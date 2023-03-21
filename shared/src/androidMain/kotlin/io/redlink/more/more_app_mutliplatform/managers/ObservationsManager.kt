package io.redlink.more.more_app_mutliplatform.managers

import io.redlink.more.more_app_mutliplatform.observations.permissions.ObservationSystemPermissions

class ObservationsManager {
    companion object {
        const val GPS_OBSERVER_TYPE = "gps-mobile-observation"
        const val POLAR_VERITY_HR_OBSERVER_TYPE = "polar-verity-observation"
        const val ACCELEROMETER_OBSERVER_TYPE = "acc-mobile-observation"
        const val QUESTION_OBSERVER_TYPE = "question-observation"

        private fun getPermsForType(observationType: String): Set<String>? =
            // get ObsFactory and get all permissions according to the existing entities
            when (observationType) {
                ACCELEROMETER_OBSERVER_TYPE -> ObservationSystemPermissions.ACCELEROMETER_OBSERVER_PERMISSIONS.permissions
                POLAR_VERITY_HR_OBSERVER_TYPE -> ObservationSystemPermissions.POLAR_VERITY_HR_OBSERVER_PERMISSIONS.permissions
                GPS_OBSERVER_TYPE -> ObservationSystemPermissions.GPS_OBSERVER_PERMISSIONS.permissions
                QUESTION_OBSERVER_TYPE -> ObservationSystemPermissions.QUESTION_OBSERVER_PERMISSIONS.permissions
                else -> null
            }

        private fun getPermissionsForObservation(observationType: String): Set<String> {
            val permissions = getPermsForType(observationType)
            if (permissions != null) {
                return permissions
            }
            return emptySet()
        }

        fun getAllPermissionsForFactories(observationTypes: List<String>): Set<String> {
            return observationTypes.flatMap { getPermissionsForObservation(it) }.toSet()
        }
    }
}