//
//  Hk_ExerciseObservation.swift
//  iosApp
//
//  Created by Masek Gergely on 19.08.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//
import Foundation
import HealthKit
import UIKit
import shared

class Hk_ExerciseObservation: HealthkitBase {
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
        super.init(observationType: HealthkitType_exercise())
    }
    
    override func fetchData() {
        print("!!!!!!!!! CALLING WORKOUT FETCH !!!!!")
        let workoutType = HKObjectType.workoutType()

        let query = HKSampleQuery(
            sampleType: workoutType,
            predicate: predicate,
            limit: HKObjectQueryNoLimit,
            sortDescriptors: [NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: true)]
        ) { query, results, error in
            if let error = error {
                print("Error fetching workouts: \(error.localizedDescription)")
                return
            }

            guard let workouts = results as? [HKWorkout] else {
                print("No workouts found")
                return
            }

            for workout in workouts {
                let start = workout.startDate.timeIntervalSince1970   // convert Date to timestamp
                let end = workout.endDate.timeIntervalSince1970
                let calories = workout.totalEnergyBurned?.doubleValue(for: HKUnit.kilocalorie()) ?? 0
                let distance = workout.totalDistance?.doubleValue(for: HKUnit.meter()) ?? 0
                let type = workout.workoutActivityType.rawValue       // convert enum to Int

                let data: [String: Any] = [
                    "WorkoutType": type,
                    "Calories": calories,
                    "Distance": distance,
                    "Start": start,
                    "End": end
                ]

                self.storeData(data: data, timestamp: -1) {}
                print("Workout: \(type) from \(start) to \(end), Calories: \(calories), Distance: \(distance)m")
            }
        }

        healthStore.execute(query)
    }
}
