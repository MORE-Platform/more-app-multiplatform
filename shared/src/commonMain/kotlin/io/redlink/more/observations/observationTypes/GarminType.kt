package io.redlink.more.observations.observationTypes

class GarminType : ObservationType("garmin-observation", emptySet(), prefix = PREFIX) {
    companion object {
        const val PREFIX = "garmin-"
    }
}