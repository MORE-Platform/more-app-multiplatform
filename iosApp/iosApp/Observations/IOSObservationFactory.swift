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
    override init(dataManager: ObservationDataManager) {
        super.init(dataManager: dataManager)
        observations.add(GPSObservation(sensorPermissions: ["gpsAlways"]))
        observations.add(AccelerometerBackgroundObservation(sensorPermissions: ["cmsensorrecorder"]))
        observations.add(PolarVerityHeartRateObservation(sensorPermissions: ["bluetoothAlways"]))
    }
}
