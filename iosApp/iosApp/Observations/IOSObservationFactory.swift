//
//  iOSObservationFactory.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

class IOSObservationFactory: ObservationFactory {

    override init(dataManager: ObservationDataManager) {
        super.init(dataManager: dataManager)
//        observations.add(AccelerometerObservation(sensorPermission: []))
        observations.add(GPSObservation(sensorPermissions: ["gpsAlways"]))
        observations.add(AccelerometerBackgroundObservation(sensorPermissions: ["cmsensorrecorder"]))
        observations.add(PolarVerityHeartRateObservation(sensorPermissions: ["bluetoothAlways"]))
    }
}
