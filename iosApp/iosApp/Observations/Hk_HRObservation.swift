//
//  Hk_HRObservation.swift
//  iosApp
//
//  Created by Masek Gergely on 19.08.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import Foundation
import HealthKit
import shared
import UIKit

class Hk_HRObservation: HealthkitBase {
    let healthStore: HKHealthStore

    let now: Date
    var startDate: Date
    var predicate: NSPredicate {
        HKQuery.predicateForSamples(withStart: startDate, end: now, options: .strictStartDate)
    }

    init(repository: MainRepository) {
        healthStore = HKHealthStore()
        now = Date()
        startDate = Calendar.current.date(byAdding: .day, value: -1, to: now)!
        print("Observation initialized")
        // must init with the observation type set
        super.init(repos: repository, observationType: HealthKitType_HR())
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
        print("!!!!!!!!! CALLING HR FETCH !!!!!")
        guard let hrType = HKObjectType.quantityType(forIdentifier: .heartRate) else {
            print("Sleep type not available")
            return
        }

        let query = HKSampleQuery(
            sampleType: hrType,
            predicate: predicate,
            limit: HKObjectQueryNoLimit,
            sortDescriptors: [NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: true)]
        ) { _, results, error in
            if let error = error {
                print("Error fetching HR samples: \(error.localizedDescription)")
                return
            }

            guard let results = results as? [HKQuantitySample] else {
                print("No HR samples found")
                return
            }
            var hrrecords = []
            for sample in results {
                let start = sample.startDate.formattedString(dateFormat: "yyyy-MM-dd:HH:mm")
                let end = sample.endDate.formattedString(dateFormat: "yyyy-MM-dd:HH:mm")
                let bpm = sample.quantity.doubleValue(for: HKUnit(from: "count/min"))

                let item: [String: Any] = [
                    "start": start,
                    "end": end,
                    "bpm": bpm,
                ]
                print("Heart Rate: \(bpm) bpm from \(start) to \(end)")
                hrrecords.append(item)
            }
            let data: [String: Any] = ["hr_records": hrrecords]
            self.storeData(data: data, timestamp: -1) {}
        }

        healthStore.execute(query)
    }

    override func ableToAutomaticallyStart() -> Bool {
        return true
    }
}
