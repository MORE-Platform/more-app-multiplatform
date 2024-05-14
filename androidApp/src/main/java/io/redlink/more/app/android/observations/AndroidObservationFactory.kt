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

import android.Manifest
import android.content.Context
import io.redlink.more.app.android.observations.GPS.GPSObservation
import io.redlink.more.app.android.observations.GPS.GPSService
import io.redlink.more.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.app.android.observations.accelerometer.AccelerometerObservation
import io.redlink.more.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.more.app.android.services.sensorsListener.GPSStateListener
import io.redlink.more.more_app_mutliplatform.observations.ObservationDataManager
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Dispatchers

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

        Scope.launch(Dispatchers.IO) {
            super.studyObservationTypes.collect { studyObservationTypes ->
                val permissions =
                    super.observations.filter { it.observationType.observationType in studyObservationTypes }
                        .flatMap { it.observationType.sensorPermissions }.toSet()
                if (permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                    || permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)
                ) {
                    GPSStateListener.startListening(context)
                } else {
                    GPSStateListener.stopListening(context)
                }
                if (permissions.contains(Manifest.permission.BLUETOOTH_SCAN)
                    || permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)
                    || permissions.contains(Manifest.permission.BLUETOOTH)
                    || permissions.contains(Manifest.permission.BLUETOOTH_ADMIN)
                ) {
                    BluetoothStateListener.startListening(context)
                } else {
                    BluetoothStateListener.stopListening(context)
                }
            }
        }
    }

}