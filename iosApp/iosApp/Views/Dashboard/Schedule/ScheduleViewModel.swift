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
    @Published var schedules: [Int64 : [ScheduleModel]] = [:] {
        didSet {
            self.scheduleDates = Array(self.schedules.keys).sorted()
        }
    }
    @Published var scheduleDates: [Int64] = []
    
    init(observationFactory: IOSObservationFactory) {
        recorder.updateTaskStates()
        coreModel = CoreScheduleViewModel(dataRecorder: recorder)
        coreModel.onScheduleModelListChange { [weak self] scheduleMap in
            let states = scheduleMap.flatMap({$0.value}).filter{ $0.scheduleState == ScheduleState.active || $0.scheduleState == ScheduleState.running || $0.scheduleState == ScheduleState.paused}
            if let self {
                print("New scheduleMap: \(states)")
                for (key, value) in scheduleMap {
                    self.schedules[Int64(truncating: key)]?.removeAll()
                    self.schedules[Int64(truncating: key)] = value
                }
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
