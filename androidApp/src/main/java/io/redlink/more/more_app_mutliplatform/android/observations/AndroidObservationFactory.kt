package io.redlink.more.more_app_mutliplatform.android.observations

import android.content.Context
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService

class AndroidObservationFactory(context: Context): ObservationFactory(AndroidObservationDataManager(context)) {
    init {
        observations.addAll(setOf(
            AccelerometerObservation(context)
        ))
    }

}