package io.redlink.more.more_app_mutliplatform.observations.ObservationTypes

import io.redlink.more.more_app_mutliplatform.observations.ObservationTypeImpl

class GPSType(sensorPermissions: Set<String>): ObservationTypeImpl("gps-mobile-observation", sensorPermissions) {
}