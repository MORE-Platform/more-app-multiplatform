//
//  Hk_SleepObservation.swift
//  iosApp
//
//  Created by Masek Gergely on 19.08.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import Foundation
import HealthKit
import shared
import UIKit

class Hk_SleepObservation: HealthkitBase {
    let healthStore: HKHealthStore

    let now: Date
    var startDate: Date
    var predicate: NSPredicate {
        HKQuery.predicateForSamples(withStart: startDate, end: now, options: .strictStartDate)
    }

    init(database: AppDatabase) {
        healthStore = HKHealthStore()
        now = Date()
        startDate = Calendar.current.date(byAdding: .day, value: -1, to: now)!
        print("Observation initialized")
        // must init with the observation type set
        super.init(database: database, observationType: HealtkitType_Sleep())
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
                        startDate = newDate
                    }
                }
            }
        } catch {
            print(error.localizedDescription)
        }
    }

    override func fetchData() {
        print("!!!!!!!!! CALLING Sleep FETCH !!!!!")
        guard let sleepType = HKObjectType.categoryType(forIdentifier: .sleepAnalysis) else {
            print("Sleep type not available")
            return
        }

        let query = HKSampleQuery(
            sampleType: sleepType,
            predicate: predicate,
            limit: HKObjectQueryNoLimit,
            sortDescriptors: [NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: true)]
        ) { _, results, error in
            if let error = error {
                print("Error fetching sleep samples: \(error.localizedDescription)")
                return
            }

            guard let results = results as? [HKCategorySample] else {
                print("No sleep samples found")
                return
            }
            var sleepData: [[String: Any]] = []

            for sample in results {
                let state: String
                switch sample.value {
                case HKCategoryValueSleepAnalysis.inBed.rawValue:
                    state = "InBed"
                case HKCategoryValueSleepAnalysis.asleep.rawValue:
                    state = "Asleep"
                default:
                    state = "Unknown"
                }

                // Convert dates to string
                let startString = sample.startDate.formattedString(dateFormat: "yyyy-MM-dd:HH:mm")
                let endString = sample.endDate.formattedString(dateFormat: "yyyy-MM-dd:HH:mm")

                // Create dictionary for this sample
                let sampleData: [String: Any] = [
                    "state": state,
                    "start": startString,
                    "end": endString,
                ]
                sleepData.append(sampleData)
            }
            let data: [String: Any] = ["sleepData": sleepData]
            self.storeData(data: data, timestamp: -1) {}
        }

        healthStore.execute(query)
    }
}
