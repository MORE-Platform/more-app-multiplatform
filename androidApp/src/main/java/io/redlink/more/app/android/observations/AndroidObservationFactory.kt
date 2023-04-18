package io.redlink.more.more_app_mutliplatform.android.observations

import android.content.Context
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidObservationDataManager
import io.redlink.more.more_app_mutliplatform.android.observations.GPS.GPSObservation
import io.redlink.more.more_app_mutliplatform.android.observations.GPS.GPSService
import io.redlink.more.more_app_mutliplatform.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.more_app_mutliplatform.android.observations.accelerometer.AccelerometerObservation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.simpleQuestionObservation.SimpleQuestionObservation

class AndroidObservationFactory(context: Context) :
    ObservationFactory(AndroidObservationDataManager(context)) {
    init {
        observations.addAll(
            setOf(
                AccelerometerObservation(context),
                GPSObservation(context, gpsService = GPSService(context)),
                SimpleQuestionObservation(),
                PolarHeartRateObservation(context)
            )
        )
    }

}