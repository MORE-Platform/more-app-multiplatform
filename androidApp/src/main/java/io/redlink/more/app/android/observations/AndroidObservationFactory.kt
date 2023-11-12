/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
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