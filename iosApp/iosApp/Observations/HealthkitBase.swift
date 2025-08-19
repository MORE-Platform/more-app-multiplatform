//
//  HealthkitBase.swift
//  iosApp
//
//  Created by Masek Gergely on 19.08.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import Foundation
import HealthKit
import UIKit
import shared



class HealthkitBase : Observation_{
    
    
    
    
    
    override func start() -> Bool {
        if #available(iOS 15.0, *) {
            requestAuthorization { [weak self] authorized in
                guard let self = self else { return }
                
                if !authorized {
                    print("HealthKit authorization denied")
                    return
                }
                
                print("HealthKit authorized, fetching sleep data...")
                self.fetchData()
            }
            return true
        }
            return false
    }
    
    func fetchData() {
        fatalError("fetchData must be overridden")
    }
    
    override func stop(onCompletion: @escaping () -> Void) {
        onCompletion()
    }

    override func applyObservationConfig(settings: Dictionary<String, Any>) {
        // Implement configuration if needed
    }
    
    /// !!!!!!!!!! ONLY HERE WHILE RUNNING APP ON EMULATOR 
    @available(iOS 15.0, *)
    private func requestAuthorization(completion: @escaping (Bool) -> Void) {
        let healthStore: HKHealthStore =  HKHealthStore()
        guard HKHealthStore.isHealthDataAvailable() else {
            print("HealthKit not available")
            completion(false)
            return
        }

        let typesArray: [HKSampleType] = [
            HKObjectType.workoutType(),
            HKQuantityType(.activeEnergyBurned),
            HKQuantityType(.distanceCycling),
            HKQuantityType(.distanceWalkingRunning),
            HKQuantityType(.distanceWheelchair),
            HKQuantityType(.heartRate),
            HKQuantityType(.stepCount),
            HKObjectType.categoryType(forIdentifier: .sleepAnalysis)!
        ].compactMap { $0 as HKSampleType }

        var allTypes = Set(typesArray)


        if let sleepType = HKObjectType.categoryType(forIdentifier: .sleepAnalysis) {
            allTypes.insert(sleepType)
        }

        healthStore.requestAuthorization(toShare: [], read: allTypes) { success, error in
            if let error = error {
                print("Authorization error: \(error.localizedDescription)")
            }
            completion(success)
        }
    }
}
