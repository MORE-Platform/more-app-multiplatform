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
package io.redlink.umm.blendedcare.app.android.observations

import android.Manifest
import android.content.Context
import io.redlink.umm.blendedcare.app.android.observations.GPS.GPSObservation
import io.redlink.umm.blendedcare.app.android.observations.GPS.GPSService
import io.redlink.umm.blendedcare.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.umm.blendedcare.app.android.observations.accelerometer.AccelerometerObservation
import io.redlink.umm.blendedcare.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.umm.blendedcare.app.android.services.sensorsListener.GPSStateListener
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.observations.ObservationDataManager
import io.redlink.umm.participant.observations.ObservationFactory
import io.redlink.umm.participant.scopes.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidObservationFactory(
    context: Context,
    observationDataManager: ObservationDataManager,
    repository: MainRepository
) :
    ObservationFactory(repository, observationDataManager) {
    init {
        observations.addAll(
            setOf(
                AccelerometerObservation(context, repository),
                GPSObservation(context, repository, gpsService = GPSService(context)),
                PolarHeartRateObservation(repository)
            )
        )

        Scope.launch(Dispatchers.IO) {
            GPSStateListener.gpsEnabled.collect {
                withContext(Dispatchers.Main) {
                    super.updateObservationErrors()
                }
            }
        }

        Scope.launch(Dispatchers.IO) {
            BluetoothStateListener.bluetoothEnabled.collect {
                withContext(Dispatchers.Main) {
                    super.updateObservationErrors()
                }
            }
        }

        Scope.launch(Dispatchers.IO) {
            super.studyObservationTypes.collect { studyObservationTypes ->
                val permissions =
                    super.observations.filter { it.observationType.observationType in studyObservationTypes }
                        .flatMap { it.observationType.sensorPermissions }.toSet()
                if (permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                    || permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)
                    || permissions.contains(Manifest.permission.BLUETOOTH_SCAN)
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