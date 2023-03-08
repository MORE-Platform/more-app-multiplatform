package io.redlink.more.more_app_mutliplatform.android.observations

import android.content.Context
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService

class AndroidObservationFactory(context: Context, networkService: NetworkService): ObservationFactory(networkService) {
    override val observations: Set<Observation>

    init {
        observations = setOf(
            AccelerometerObservation(context, "acc-mobile-observation")
        )
    }

}