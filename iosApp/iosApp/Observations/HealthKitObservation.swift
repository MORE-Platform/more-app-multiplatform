//
//  HealthKitObservation.swift
//  iosApp
//
//  Created by Masek Gergely on 31.07.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import CoreLocation
import Foundation
import shared
import UIKit
import HealthKit

class HealthKitObservation: Observation_ {
    
    let heatlhkStore: HKHealthStore!
    
    init(){
        if HKHealthStore.isHealthDataAvailable() {
           let healtkitStore = HKHealthStore()
        }
        else{
            throw exception("Health kit not available")
        }
    }
    
    
    
    
    
    
}
