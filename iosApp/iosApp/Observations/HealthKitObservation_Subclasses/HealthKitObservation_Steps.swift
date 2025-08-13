//
//  HealthKitObservation_Steps.swift
//  iosApp
//
//  Created by Masek Gergely on 13.08.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

class HealthKitObservation_Steps: HealthKitObservation {
    let stepType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
    let dataTypes: Set<HKSampleType> = [stepType]
    
    
    @available(iOS 15.0, *)
    override func start() async -> Bool {
        // Ensure HealthKit is available
        guard HKHealthStore.isHealthDataAvailable() else {
            print("HealthKit not available")
            return false
        }
        
        do {
            // Request authorization asynchronously
            try await healthStore.requestAuthorization(toShare: nil, read: dataTypes)
            
            
            
            guard let stepType = HKQuantityType.quantityType(forIdentifier: .stepCount) else {
                print("Step count type is unavailable")
                return false
            }
            
            // Use a continuation to wait for the async query result
            let stepObjects: [StepRecord] = try await withCheckedThrowingContinuation { continuation in
                let query = HKSampleQuery(sampleType: stepType,
                                          predicate: predicate,
                                          limit: HKObjectQueryNoLimit,
                                          sortDescriptors: nil) { query, samples, error in
                    if let error = error {
                        continuation.resume(throwing: error)
                        return
                    }
                    
                    guard let stepSamples = samples as? [HKQuantitySample] else {
                        continuation.resume(returning: [])
                        return
                    }
                    
                    let steps = stepSamples.map { sample in
                        StepRecord(startDate: sample.startDate,
                                   endDate: sample.endDate,
                                   steps: sample.quantity.doubleValue(for: HKUnit.count()))
                    }
                    
                    continuation.resume(returning: steps)
                }
                
                healthStore.execute(query)
            }
            
            // Store or use the results internally
            self.stepRecords = stepObjects
            print("Fetched \(stepObjects.count) step records")
            //TODO SEND DATA to Gateway
            return true
        } catch {
            print("Authorization or query failed: \(error.localizedDescription)")
            return false
        }
    }

   

    
}
