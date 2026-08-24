/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
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
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.observations.Observation
import io.redlink.more.observations.ObservationDataManager
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.scopes.AppDispatchers
import io.redlink.more.scopes.MoreScope
import io.redlink.more.scopes.Scope
import io.redlink.more.services.store.SharedStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidObservationFactory(
    context: Context,
    observationDataManager: ObservationDataManager,
    repository: MainRepository,
    sharedStorageRepository: SharedStorageRepository,
    scope: MoreScope = Scope
) :
    ObservationFactory(
        repository,
        sharedStorageRepository,
        observationDataManager
    ) {
    init {
        registerObservation {
            AccelerometerObservation(context, repository)
        }
        registerObservation {
            GPSObservation(context, repository, gpsService = GPSService(context))
        }
        registerObservation {
            PolarHeartRateObservation(repository)
        }
        registerObservation {
            appUsageObservation!!
        }

        scope.launch(AppDispatchers.io) {
            GPSStateListener.gpsEnabled.collect {
                withContext(Dispatchers.Main) {
                    super.updateObservationErrors()
                }
            }
        }

        scope.launch(AppDispatchers.io) {
            BluetoothStateListener.bluetoothEnabled.collect {
                withContext(Dispatchers.Main) {
                    super.updateObservationErrors()
                }
            }
        }

        scope.launch(AppDispatchers.io) {
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

    override fun observationPostConstruct(observation: Observation) {
        observation.setPermissionObserver(
            AndroidObservationPermissionObserver(
                permissionRepository
            )
        )
    }
}