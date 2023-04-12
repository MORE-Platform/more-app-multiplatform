//
//  ObservationDataCollector.swift
//  iosApp
//
//  Created by Jan Cortiel on 29.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared


class ObservationDataCollector {
    private let observationFactory = IOSObservationFactory()
    private let dataManager: iOSObservationDataManager
    private let observationRepository: ObservationRepository
    private let scheduleRepository = ScheduleRepository()
    private var job: Ktor_ioCloseable?
    
    init() {
        let realm = RealmDatabase()
        dataManager = iOSObservationDataManager()
        observationRepository = ObservationRepository()
    }

    func collectData(dataCollected completion: @escaping (Bool) -> Void) {
        print("Collect undone observations")
        job = observationRepository.collectObservationsWithUndoneSchedules { [weak self] observations in
            if let self {
                self.scheduleRepository.updateTaskStates(observationFactory: nil)
                let observationMergerSet = observations
                    .mapValues{$0.filter{$0.getState() == .running}}
                    .filter{!$0.value.isEmpty}

                print("Schedule Set is empty: \(observationMergerSet.isEmpty)")
                if !observationMergerSet.isEmpty {
                    let observationTypeSet = Set(observationMergerSet.keys.map { $0.observationType })
                    print("Observation Type set \(observationTypeSet)")
                    var i = 0
                    observationTypeSet.forEach { type in
                        print("Collecting type \(type)")
                        let obsMerger = observationMergerSet.flatMap{ $0.value}.filter{ $0.observationType == type }
                        
                        if !obsMerger.isEmpty,
                           let obs = self.observationFactory.observation(type: type),
                           let start = observations.keys.filter({$0.observationType == type}).map({Int64($0.collectionTimestamp.epochSeconds)}).max(),
                           let end = obsMerger.map({Int64($0.end?.epochSeconds ?? 0)}).max() {
                            
                            obsMerger.forEach { obs.apply(observationId: $0.observationId, scheduleId: $0.scheduleId.toHexString()) }
                            
                            let now = Int64(Date().timeIntervalSince1970)
                            obs.store(start: start, end: end > now ? now : end) {
                                print("\(Date()): Stored data")
                                self.observationRepository.lastCollection(type: type, timestamp: now)
                                
                                obsMerger.forEach { obs.remove(observationId: $0.observationId, scheduleId: $0.scheduleId.toHexString()) }
                                
                                i += 1
                                if i == observationTypeSet.count {
                                    print("\(Date())Returning from collected")
                                    completion(true)
                                }
                            }
                        }
                    }
                } else {
                    completion(false)
                }
            } else {
                print("Cannot find self")
                completion(false)
            }
        }
    }

    func close() {
        print("Observation Collector closed!")
        job?.close()
        dataManager.close()
    }
}
