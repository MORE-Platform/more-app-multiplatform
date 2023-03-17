//
//  iOSObservationFactory.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

class IOSObservationFactory: ObservationFactory {
    
    
    init() {
        super.init(networkService: NetworkService.create())
        observations.add(AccelerometerObservation(sensorPermission: []))
    }
}