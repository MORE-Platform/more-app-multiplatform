//
//  HealthKitHelper.swift
//  iosApp
//
//  Created by Masek Gergely on 16.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//
import HealthKit


class HealthKitHelper:ObservableObject{
    
    public var healthStore: HKHealthStore = .init()
    public let healthKitTypes: [HKObjectType] = []
    @Published public var isAvailable: Bool = false
    public var allTypes: Set<HKSampleType> = []
    
    init(){
        if !HKHealthStore.isHealthDataAvailable( ){
            isAvailable = false
        }
        else {
            isAvailable = true
            
            if #available(iOS 15.0, *) {
                allTypes = Set([
                    HKObjectType.workoutType(),
                    HKQuantityType(.activeEnergyBurned),
                    HKQuantityType(.distanceCycling),
                    HKQuantityType(.distanceWalkingRunning),
                    HKQuantityType(.distanceWheelchair),
                    HKQuantityType(.heartRate),
                    HKQuantityType(.stepCount)
                ].compactMap { $0 as HKSampleType })
                
            }
            else {
                allTypes = []
                isAvailable = false
            }
            
        }
    }
    @available(iOS 15.0, *)
    func authorize() async throws {
        do{
            try await healthStore.requestAuthorization(toShare: allTypes, read: allTypes)
        }
        catch{
            fatalError("HEALTH KIT CONNECTION FAILED")
        }
        
    }
    @available(iOS 15.4, *)
    func getSteps() async throws -> Double{
        
        let calendar = Calendar(identifier: .gregorian)
        let startDate = calendar.startOfDay(for: Date())
        let endDate = calendar.date(byAdding: .day, value: 1, to: startDate)
        let today = HKQuery.predicateForSamples(withStart: startDate, end: endDate)


        // Create the query descriptor.
        let stepType = HKQuantityType(.stepCount)
        let stepsToday = HKSamplePredicate.quantitySample(type: stepType, predicate:today)
        let sumOfStepsQuery = HKStatisticsQueryDescriptor(predicate: stepsToday, options: .cumulativeSum)
       

        // Run the query.
        do{
            let stepCount = try await sumOfStepsQuery.result(for: self.healthStore)?
                .sumQuantity()?
                .doubleValue(for: HKUnit.count())
            return stepCount ?? 0
        }
        catch  {
            print("ERROR")
        }
        
        return 0.0
    }
    /// Generic funtion to get any kind of data from health kit that are allowed in permissions
    ///
    
    @available(iOS 15.4 , *)
    func getData(datatype : HKQuantityType, daysback: Int = 1) async throws -> Any{
        let calendar = Calendar(identifier: .gregorian)
        var endDate  = calendar.startOfDay(for: Date())
        let startDate = calendar.date(byAdding: .day,value: -daysback ,to: endDate)
        endDate = calendar.date(byAdding: .day, value: 1, to: endDate)!
        let today = HKQuery.predicateForSamples(withStart: startDate, end: endDate)
        let data_today = HKSamplePredicate.quantitySample(type: datatype, predicate:today)
        let query = HKStatisticsQueryDescriptor(predicate: data_today, options: .cumulativeSum)
        
        do {
            let result = try await query.result(for: self.healthStore)?
                .sumQuantity()?
                .doubleValue(for: HKUnit.count())
            print(result)
            return result
        }
        catch{
            print("error fetching data")
            return  0.0
        }
    }
    
    func saveSteps(count: Double, date: Date) async throws {
            let type = HKQuantityType.quantityType(forIdentifier: .stepCount)!
            let quantity = HKQuantity(unit: HKUnit.count(), doubleValue: count)
            let sample = HKQuantitySample(type: type, quantity: quantity, start: date, end: date)
            try await healthStore.save(sample)
        }

        func saveHeartRate(bpm: Double, date: Date) async throws {
            let type = HKQuantityType.quantityType(forIdentifier: .heartRate)!
            let quantity = HKQuantity(unit: HKUnit.count().unitDivided(by: .minute()), doubleValue: bpm)
            let sample = HKQuantitySample(type: type, quantity: quantity, start: date, end: date)
            try await healthStore.save(sample)
        }

        func saveSleep(start: Date, end: Date) async throws {
            let type = HKCategoryType.categoryType(forIdentifier: .sleepAnalysis)!
            let sample = HKCategorySample(type: type,
                                          value: HKCategoryValueSleepAnalysis.asleep.rawValue,
                                          start: start,
                                          end: end)
            try await healthStore.save(sample)
        }

        func saveWorkout(start: Date, end: Date, calories: Double = 200) async throws {
            let workout = HKWorkout(activityType: .running,
                                    start: start,
                                    end: end,
                                    workoutEvents: nil,
                                    totalEnergyBurned: HKQuantity(unit: .kilocalorie(), doubleValue: calories),
                                    totalDistance: HKQuantity(unit: .meter(), doubleValue: 3000),
                                    device: .local(),
                                    metadata: nil)
            try await healthStore.save(workout)
        }
}
