//
//  iOSObservationFactory.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import Foundation
import shared

class IOSObservationFactory: ObservationFactory {
    init(repository: MainRepository, dataManager: ObservationDataManager, userDefaults: SharedStorageRepository) {
        super.init(repository: repository, sharedStorageRepository: userDefaults, dataManager: dataManager, scope: Scope.shared)
        registerObservation {
            GPSObservation(repos: repository, sensorPermissions: ["gpsAlways"])
        }

        registerObservation {
            AccelerometerBackgroundObservation(repos: repository, sensorPermissions: ["cmsensorrecorder"])
        }

        registerObservation {
            PolarVerityHeartRateObservation(repos: repository, sensorPermissions: ["bluetoothAlways"])
        }
    }

    override func observationPostConstruct(observation: Observation_) {
        observation.setPermissionObserver(observer: IOSObservationPermissionObserver())
    }
}
