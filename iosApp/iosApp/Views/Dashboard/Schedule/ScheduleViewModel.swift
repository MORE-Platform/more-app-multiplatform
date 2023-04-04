//
//  ScheduleViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class ScheduleViewModel: ObservableObject {
    let recorder = IOSDataRecorder()
    private let coreModel: CoreScheduleViewModel
    @Published var schedules: [UInt64 : [ScheduleModel]] = [:]
    @Published var scheduleDates: [UInt64] = []
    
    init(observationFactory: IOSObservationFactory) {
        recorder.updateTaskStates()
        coreModel = CoreScheduleViewModel(dataRecorder: recorder)
        coreModel.onScheduleModelListChange { scheduleMap in
            DispatchQueue.main.async {
                for (key, value) in scheduleMap {
                    self.schedules[UInt64(truncating: key)] = value
                }
                self.scheduleDates = Array(self.schedules.keys).sorted()
            }
        }
    }
    
    func start(scheduleId: String) {
        coreModel.start(scheduleId: scheduleId)
    }
    
    func pause(scheduleId: String) {
        coreModel.pause(scheduleId: scheduleId)
    }
    
    func stop(scheduleId: String) {
        coreModel.stop(scheduleId: scheduleId)
    }
}
