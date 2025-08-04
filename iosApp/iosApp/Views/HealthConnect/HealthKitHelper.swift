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
}
