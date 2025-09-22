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
    var startDate: Date
    var predicate: NSPredicate {
        HKQuery.predicateForSamples(withStart: startDate, end: now, options: .strictStartDate)
    }
    
    init(database: AppDatabase) {
        self.healthStore = HKHealthStore()
        self.now = Date()
        self.startDate = Calendar.current.date(byAdding: .day, value: -1, to: now)!
        print("Observation initialized")
        //must init with the observation type set
        super.init(database: database, observationType: HealthkitType_steps())
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
                let start = sample.startDate.formattedString(dateFormat: "yyyy-MM-dd:HH:mm")
                let end = sample.endDate.formattedString(dateFormat: "yyyy-MM-dd:HH:mm")
                let steps = sample.quantity.doubleValue(for: HKUnit.count())
                print("Steps: \(steps) from \(start) to \(end)")
            }
            
            // Optional: total steps
            let totalSteps = results.reduce(0.0) { $0 + $1.quantity.doubleValue(for: HKUnit.count()) }
            let data: [String: Any] = ["Steps" : totalSteps, "Start": self.startDate.formattedString(dateFormat: "yyyy-MM-dd:HH:mm"), "End": self.now.formattedString(dateFormat: "yyyy-MM-dd:HH:mm")]
                    print("Total steps in time range: \(totalSteps)")
            self.storeData(data: data, timestamp: -1){}
        }
        healthStore.execute(query)
    }
    override func applyObservationConfig(settings: Dictionary<String, Any>) {
        do {
            print("observation config failed")
                if let daysBackValue = settings["daysback"] {
                    // Convert value to String, strip quotes, then to Int
                    let strValue = String(describing: daysBackValue).trimmingCharacters(in: CharacterSet(charactersIn: "\""))
                    print(strValue)
                    print("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
                    if let daysBack = Int(strValue) {
                        print(daysBack)
                        
                        // Subtract days from current date
                        if let newDate = Calendar.current.date(byAdding: .day, value: -daysBack, to: Date()) {
                            self.startDate = newDate
                        }
                    }
                }
            } catch {
                print(error.localizedDescription)
            }    }
}
