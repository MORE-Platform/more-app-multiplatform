package io.redlink.more.app.android.observations

import android.content.Context
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.observations.GPS.GPSObservation
import io.redlink.more.app.android.observations.GPS.GPSService
import io.redlink.more.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.app.android.observations.accelerometer.AccelerometerObservation
import io.redlink.more.more_app_mutliplatform.observations.ObservationDataManager
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory

class AndroidObservationFactory(context: Context, observationDataManager: ObservationDataManager) :
    ObservationFactory(observationDataManager) {
    init {
        observations.addAll(
            setOf(
                AccelerometerObservation(context),
                GPSObservation(context, gpsService = GPSService(context)),
                PolarHeartRateObservation()
            )
        )
    }

}