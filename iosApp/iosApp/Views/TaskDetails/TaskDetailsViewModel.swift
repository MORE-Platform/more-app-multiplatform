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

    @Published var observationRepetitionInterval: String = "1x/week"
     
    
    init(dataRecorder: IOSDataRecorder, observationId: String, scheduleId: String) {
        self.coreModel = CoreTaskDetailsViewModel(dataRecorder: dataRecorder)
        coreModel.onLoadTaskDetails(observationId: observationId, scheduleId: scheduleId) { taskDetails in
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
