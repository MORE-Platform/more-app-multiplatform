//
//  Hk_ExerciseObservation.swift
//  iosApp
//
//  Created by Masek Gergely on 19.08.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//
import Foundation
import HealthKit
import shared
import UIKit

class Hk_ExerciseObservation: HealthkitBase {
    let healthStore: HKHealthStore

    let now: Date
    var startDate: Date
    var predicate: NSPredicate {
        HKQuery.predicateForSamples(withStart: startDate, end: now, options: .strictStartDate)
    }
    private var sendRawData :Bool = false
    init(repostiory: MainRepository) {
        healthStore = HKHealthStore()
        now = Date()
        startDate = Calendar.current.date(byAdding: .day, value: -1, to: now)!
        print("Observation initialized")
        // must init with the observation type set
        super.init(repos: repostiory, observationType: HealthkitType_exercise())
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
            if let sendRawDataValue = settings["sendRawData"] {
                let strValue = String(describing: sendRawDataValue).trimmingCharacters(in: CharacterSet(charactersIn: "\""))
                sendRawData = Bool(strValue) ?? false
            }
        } catch {
            print(error.localizedDescription)
        }
    }

    override func fetchData() {
        print("!!!!!!!!! CALLING WORKOUT FETCH !!!!!")
        let workoutType = HKObjectType.workoutType()

        let query = HKSampleQuery(
            sampleType: workoutType,
            predicate: predicate,
            limit: HKObjectQueryNoLimit,
            sortDescriptors: [NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: true)]
        ) { _, results, error in
            if let error = error {
                print("Error fetching workouts: \(error.localizedDescription)")
                return
            }
            
            guard let workouts = results as? [HKWorkout] else {
                print("No workouts found")
                return
            }
            if(!self.sendRawData){
                for workout in workouts {
                    let start = workout.startDate.formattedString(dateFormat: "yyyy-MM-dd:HH:mm") // convert Date to timestamp
                    let end = workout.endDate.formattedString(dateFormat: "yyyy-MM-dd:HH:mm")
                    let calories = workout.totalEnergyBurned?.doubleValue(for: HKUnit.kilocalorie()) ?? 0
                    let distance = workout.totalDistance?.doubleValue(for: HKUnit.meter()) ?? 0
                    let type = workout.workoutActivityType
                    // HKWorkoutActivityType
                    let workoutName = WorkoutType(type).rawValue
                    let data: [String: Any] = [
                        "WorkoutType": workoutName,
                        "Calories": calories,
                        "Distance": distance,
                        "Start": start,
                        "End": end,
                    ]
                    print(data)
                    print(workout)
                    print("@@@@@")
                    self.storeData(data: data, timestamp: -1) {}
                    // print("Workout: \(type) from \(start) to \(end), Calories: \(calories), Distance: \(distance)m")
                }}
            else{
                //General mapper from parent class
                
                let jsonRawDataList = workouts.map { self.mapSampleToJSON($0) }

                // Wrap into a payload dictionary
                let data: [String: Any] = ["raw Data": jsonRawDataList]

                // Store it
                self.storeData(data: data, timestamp: -1) {}            }
        }

        healthStore.execute(query)
    }
}

// Enum wrapper for HKWorkoutActivityType
enum WorkoutType: String {
    case americanFootball = "American Football"
    case archery = "Archery"
    case australianFootball = "Australian Football"
    case badminton = "Badminton"
    case baseball = "Baseball"
    case basketball = "Basketball"
    case bowling = "Bowling"
    case boxing = "Boxing"
    case climbing = "Climbing"
    case cricket = "Cricket"
    case crossTraining = "Cross Training"
    case curling = "Curling"
    case cycling = "Cycling"
    case dance = "Dance"
    case danceInspiredTraining = "Dance Inspired Training"
    case elliptical = "Elliptical"
    case equestrianSports = "Equestrian Sports"
    case fencing = "Fencing"
    case fishing = "Fishing"
    case functionalStrengthTraining = "Functional Strength Training"
    case golf = "Golf"
    case gymnastics = "Gymnastics"
    case handball = "Handball"
    case hiking = "Hiking"
    case hockey = "Hockey"
    case hunting = "Hunting"
    case lacrosse = "Lacrosse"
    case martialArts = "Martial Arts"
    case mindfulSession = "Mindful Session"
    case mixedMetabolicCardioTraining = "Mixed Metabolic Cardio Training"
    case paddleSports = "Paddle Sports"
    case Pilates
    case pickleball = "Pickleball"
    case preparationAndRecovery = "Preparation & Recovery"
    case racquetball = "Racquetball"
    case rowing = "Rowing"
    case rugby = "Rugby"
    case running = "Running"
    case sailing = "Sailing"
    case skatingSports = "Skating Sports"
    case snowSports = "Snow Sports"
    case soccer = "Soccer"
    case softTennis = "Soft Tennis"
    case squash = "Squash"
    case stairClimbing = "Stair Climbing"
    case surfingSports = "Surfing Sports"
    case swimming = "Swimming"
    case tableTennis = "Table Tennis"
    case tennis = "Tennis"
    case trackAndField = "Track & Field"
    case traditionalStrengthTraining = "Strength Training"
    case volleyball = "Volleyball"
    case walking = "Walking"
    case waterFitness = "Water Fitness"
    case wheelchairWalkPace = "Wheelchair Walk Pace"
    case wheelchairRunPace = "Wheelchair Run Pace"
    case yoga = "Yoga"
    case other = "Other"

    // Initialize from HKWorkoutActivityType
    init(_ type: HKWorkoutActivityType) {
        switch type {
        case .americanFootball: self = .americanFootball
        case .archery: self = .archery
        case .australianFootball: self = .australianFootball
        case .badminton: self = .badminton
        case .baseball: self = .baseball
        case .basketball: self = .basketball
        case .bowling: self = .bowling
        case .boxing: self = .boxing
        case .climbing: self = .climbing
        case .cricket: self = .cricket
        case .crossTraining: self = .crossTraining
        case .curling: self = .curling
        case .cycling: self = .cycling
        case .dance: self = .dance
        case .danceInspiredTraining: self = .danceInspiredTraining
        case .elliptical: self = .elliptical
        case .equestrianSports: self = .equestrianSports
        case .fencing: self = .fencing
        case .fishing: self = .fishing
        case .functionalStrengthTraining: self = .functionalStrengthTraining
        case .golf: self = .golf
        case .gymnastics: self = .gymnastics
        case .handball: self = .handball
        case .hiking: self = .hiking
        case .hockey: self = .hockey
        case .hunting: self = .hunting
        case .lacrosse: self = .lacrosse
        case .martialArts: self = .martialArts
        case .mixedMetabolicCardioTraining: self = .mixedMetabolicCardioTraining
        case .paddleSports: self = .paddleSports
        case .pickleball: self = .pickleball
        case .preparationAndRecovery: self = .preparationAndRecovery
        case .racquetball: self = .racquetball
        case .rowing: self = .rowing
        case .rugby: self = .rugby
        case .running: self = .running
        case .sailing: self = .sailing
        case .skatingSports: self = .skatingSports
        case .snowSports: self = .snowSports
        case .soccer: self = .soccer
        case .squash: self = .squash
        case .stairClimbing: self = .stairClimbing
        case .surfingSports: self = .surfingSports
        case .swimming: self = .swimming
        case .tableTennis: self = .tableTennis
        case .tennis: self = .tennis
        case .trackAndField: self = .trackAndField
        case .traditionalStrengthTraining: self = .traditionalStrengthTraining
        case .volleyball: self = .volleyball
        case .walking: self = .walking
        case .waterFitness: self = .waterFitness
        case .wheelchairWalkPace: self = .wheelchairWalkPace
        case .wheelchairRunPace: self = .wheelchairRunPace
        case .yoga: self = .yoga
        default: self = .other
        }
    }
}
