package io.redlink.more.more_app_mutliplatform.observations.ObservationTypes

import io.redlink.more.more_app_mutliplatform.observations.ObservationTypeImpl

class AccelerometerType(sensorPermissions: Set<String>): ObservationTypeImpl("acc-mobile-observation", sensorPermissions) {
}