//
//  Hk_StepsObservation.swift
//  iosApp
//
//  Created by Masek Gergely on 19.08.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//


import Foundation
import HealthKit
import UIKit
import shared

class Hk_StepsObservation: HealthkitBase {
    let healthStore: HKHealthStore
    
    let now: Date
    let startDate: Date
    var predicate: NSPredicate {
        HKQuery.predicateForSamples(withStart: startDate, end: now, options: .strictStartDate)
    }
    
    init() {
        self.healthStore = HKHealthStore()
        self.now = Date()
        self.startDate = Calendar.current.date(byAdding: .day, value: -1, to: now)!
        print("Observation initialized")
        //must init with the observation type set
        super.init(observationType: HealthkitType_steps())
    }
    
    override func fetchData() {
        print("!!!!!!!!! CALLING Steps FETCH !!!!!")
        guard let stepsType = HKObjectType.quantityType(forIdentifier: .stepCount) else {
            print("Data not available")
            return
        }
        
        
        let query = HKSampleQuery(
            sampleType: stepsType,
            predicate: predicate,
            limit: HKObjectQueryNoLimit,
            sortDescriptors: [NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: true)]
        ){ query, results, error in
            if let error = error {
                print("Error fetching steps: \(error.localizedDescription)")
                return
            }
            
            guard let results = results as? [HKQuantitySample] else {
                print("No step count samples found")
                return
            }
            
            for sample in results {
                let start = sample.startDate.formattedString(dateFormat: "yyyy-MM-dd-HH-mm")
                let end = sample.endDate.formattedString(dateFormat: "yyyy-MM-dd-HH-mm")
                let steps = sample.quantity.doubleValue(for: HKUnit.count())
                print("Steps: \(steps) from \(start) to \(end)")
            }
            
            // Optional: total steps
            let totalSteps = results.reduce(0.0) { $0 + $1.quantity.doubleValue(for: HKUnit.count()) }
            let data: [String: Any] = ["Steps" : totalSteps]
                    print("Total steps in time range: \(totalSteps)")
            self.storeData(data: data, timestamp: -1){}
        }
        healthStore.execute(query)
    }
   
}
