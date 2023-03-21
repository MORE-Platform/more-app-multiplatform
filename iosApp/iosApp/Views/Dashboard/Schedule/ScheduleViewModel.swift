//
//  ScheduleViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class ScheduleViewModel: ObservableObject {
    
    private(set) var coreModel: CoreScheduleViewModel
    @Published var schedules: [UInt64 : [ScheduleModel]] = [:]
    @Published var scheduleDates: [UInt64] = []
    @Published var scheduleStates: [String: ScheduleState] = [:]
    
    init(observationFactory: IOSObservationFactory) {
        coreModel = CoreScheduleViewModel(dataRecorder: IOSDataRecorder())
    }
    
    func start(scheduleId: String) {
        coreModel.start(scheduleId: scheduleId)
        scheduleStates[scheduleId] = .running
    }
    
    func pause(scheduleId: String) {
        coreModel.pause(scheduleId: scheduleId)
        scheduleStates[scheduleId] = .paused
    }
    
    func stop(scheduleId: String) {
        coreModel.stop(scheduleId: scheduleId)
        scheduleStates[scheduleId] = .stopped
    }
    
    func loadData() {
        coreModel.onScheduleModelListChange { scheduleMap in
            DispatchQueue.main.async {
                for (key, value) in scheduleMap {
                    self.schedules[UInt64(truncating: key)] = value
                }
                self.scheduleDates = Array(self.schedules.keys).sorted()
            }
        }
    }
    
}
