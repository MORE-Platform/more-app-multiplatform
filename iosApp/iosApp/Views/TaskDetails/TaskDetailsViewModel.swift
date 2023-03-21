//
//  TaskDetailsViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class TaskDetailsViewModel: ObservableObject {
    private let coreModel: CoreTaskDetailsViewModel
    
    @Published var taskDetailsModel: TaskDetailsModel?
    @Published var dataPointCount: DataPointCountSchema?
    
    init(observationId: String, scheduleId: String, scheduleState: ScheduleState) {
        self.coreModel = CoreTaskDetailsViewModel(dataRecorder: IOSDataRecorder())
        coreModel.onLoadTaskDetails(observationId: observationId, scheduleId: scheduleId, scheduleState: scheduleState) { taskDetails in
            if let taskDetails {
                self.taskDetailsModel = taskDetails
            }
        }
        coreModel.onLoadDataPointCount { dataPointCount in
            if let dataPointCount {
                self.dataPointCount = dataPointCount
            }
        }
    }
}
