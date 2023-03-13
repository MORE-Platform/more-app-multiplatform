//
//  ScheduleViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class ScheduleViewModel: ObservableObject {
    
    private let coreModel: CoreScheduleViewModel
    @Published var schedules: [UInt64 : [ScheduleModel]] = [:]
    @Published var scheduleDates: [UInt64] = []
    @Published var scheduleStates: [String: ScheduleState] = [:]
    
    init(observationFactory: IOSObservationFactory) {
        coreModel = CoreScheduleViewModel(observationFactory: observationFactory)
    }
    
    func start(scheduleId: String, observationId: String, type: String) {
        coreModel.start(scheduleId: scheduleId, observationId: observationId, type: type)
    }
    
    func pause(scheduleId: String) {
        coreModel.pause(scheduleId: scheduleId)
    }
    
    func stop(scheduleId: String) {
        coreModel.stop(scheduleId: scheduleId)
    }
    
    func loadData() {
        coreModel.onScheduleStateChange { stateMap in
            self.scheduleStates += stateMap
        }
        coreModel.onScheduleModelListChange { scheduleMap in
            for (key, value) in scheduleMap {
                self.schedules[UInt64(truncating: key)] = value
            }
            self.scheduleDates = Array(self.schedules.keys).sorted()
        }
    }
    
}
