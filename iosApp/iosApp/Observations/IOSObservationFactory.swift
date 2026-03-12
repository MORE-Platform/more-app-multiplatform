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
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Foundation
import shared

class IOSObservationFactory: ObservationFactory {
    override init(repository: MainRepository, dataManager: ObservationDataManager) {
        super.init(repository: repository, dataManager: dataManager)
        observations.add(GPSObservation(repos: repository, sensorPermissions: ["gpsAlways"]))
        observations.add(AccelerometerBackgroundObservation(repos: repository, sensorPermissions: ["cmsensorrecorder"]))
        observations.add(PolarVerityHeartRateObservation(repos: repository, sensorPermissions: ["bluetoothAlways"]))
        observations.add(Polar360HrObservation(repos: repository, sensorPermissions: ["bluetoothAlways"]))
        observations.add(Polar360AccObservation(repos: repository, sensorPermissions: ["bluetoothAlways"]))
        observations.add(Polar360TempObservation(repos: repository, sensorPermissions: ["bluetoothAlways"]))
        observations.add(Polar360PpiObservation(repos: repository, sensorPermissions: ["bluetoothAlways"]))
    }
}
