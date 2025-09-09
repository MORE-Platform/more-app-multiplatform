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
    override init(database: AppDatabase ,dataManager: ObservationDataManager) {
        super.init(database: database, dataManager: dataManager)
        observations.add(GPSObservation(database: database, sensorPermissions: ["gpsAlways"]))
        observations.add(AccelerometerBackgroundObservation(database: database, sensorPermissions: ["cmsensorrecorder"]))
        observations.add(PolarVerityHeartRateObservation(database: database, sensorPermissions: ["bluetoothAlways"]))
    }
}
