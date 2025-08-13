//
//  HealthKitObservation.swift
//  iosApp
//
//  Created by Masek Gergely on 31.07.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import CoreLocation
import Foundation
import shared
import UIKit
import HealthKit

import HealthKit
import Foundation

class HealthKitObservation: Observation_ {
    
    let healthStore: HKHealthStore
    
    // Default time range (last 24 hours)
    let now: Date
    let startDate: Date
    var predicate: NSPredicate {
        HKQuery.predicateForSamples(withStart: startDate, end: now, options: .strictStartDate)
    }
    
    init() throws {
        guard HKHealthStore.isHealthDataAvailable() else {
            throw NSError(domain: "HealthKitObservation",
                          code: 1,
                          userInfo: [NSLocalizedDescriptionKey: "HealthKit not available"])
        }
        self.healthStore = HKHealthStore()
        self.now = Date()
        self.startDate = Calendar.current.date(byAdding: .day, value: -1, to: now)!
    }
    
  
    @available(iOS 15.0, *)
    func start() async -> Bool {
        fatalError("Subclasses must override start()")
    }
    
    
    func stop(onCompletion: @escaping () -> Void) {
        onCompletion()
    }
    
    
    @available(iOS 15.0, *)
    func executeQuery<T: HKSample>(_ query: HKSampleQuery) async throws -> [T] {
        try await withCheckedThrowingContinuation { continuation in
            query.resultsHandler = { _, samples, error in
                if let error = error {
                    continuation.resume(throwing: error)
                    return
                }
                
                guard let samples = samples as? [T] else {
                    continuation.resume(returning: [])
                    return
                }
                
                continuation.resume(returning: samples)
            }
            
            self.healthStore.execute(query)
        }
    }
}

