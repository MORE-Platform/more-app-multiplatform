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
    
   
    
    func mapSampleToJSON(_ sample: HKSample) -> [String: Any] {
        var dict: [String: Any] = [
            "uuid": sample.uuid.uuidString,
            "type": sample.sampleType.identifier,
            "startDate": sample.startDate.ISO8601Format(),
            "endDate": sample.endDate.ISO8601Format()
        ]
        
        // Handle specific sample subclasses
        switch sample {
        case let q as HKQuantitySample:
            let unit: HKUnit
                
                switch q.quantityType.identifier {
                case HKQuantityTypeIdentifier.heartRate.rawValue:
                    unit = HKUnit.count().unitDivided(by: .minute())
                case HKQuantityTypeIdentifier.stepCount.rawValue:
                    unit = HKUnit.count()
                case HKQuantityTypeIdentifier.distanceWalkingRunning.rawValue:
                    unit = HKUnit.meter()
                case HKQuantityTypeIdentifier.activeEnergyBurned.rawValue:
                    unit = HKUnit.kilocalorie()
                default:
                    unit = HKUnit.count() // fallback if unknown
                }
                
                dict["quantity"] = q.quantity.doubleValue(for: unit)
                dict["unit"] = unit.unitString
            
        case let c as HKCumulativeQuantitySample:
            dict["quantity"] = c.quantity.doubleValue(for: HKUnit.count())
            dict["unit"] = "count"
            
        case let d as HKDiscreteQuantitySample:
            dict["average"] = d.averageQuantity.doubleValue(for: HKUnit.count())
            dict["min"] = d.minimumQuantity.doubleValue(for: HKUnit.count())
            dict["max"] = d.maximumQuantity.doubleValue(for: HKUnit.count())
            dict["unit"] = "count"
            
        case let cat as HKCategorySample:
            dict["value"] = cat.value
            dict["categoryType"] = cat.categoryType.identifier
            
        case let workout as HKWorkout:
            dict["workoutType"] = workout.workoutActivityType.rawValue
            dict["duration"] = workout.duration
            dict["totalEnergyBurned"] = workout.totalEnergyBurned?.doubleValue(for: .kilocalorie())
            dict["totalDistance"] = workout.totalDistance?.doubleValue(for: .meter())
            
        default:
            dict["description"] = sample.description
        }
        
        // Attach metadata if available
        if let metadata = sample.metadata {
            dict["metadata"] = metadata.mapValues { "\($0)" } // stringify values
        }
        
        return dict
    }
    
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
    
    override func ableToAutomaticallyStart() -> Bool {
        return true
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
