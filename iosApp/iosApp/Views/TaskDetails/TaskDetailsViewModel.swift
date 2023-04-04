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

    @Published var observationRepetitionInterval: String = "1x/week"
     
    
    init(observationId: String, scheduleId: String, dataRecorder: IOSDataRecorder) {
        self.coreModel = CoreTaskDetailsViewModel(scheduleId: scheduleId, dataRecorder: dataRecorder)
        coreModel.onLoadTaskDetails { taskDetails in
            if let taskDetails {
                self.taskDetailsModel = taskDetails
            }
        }
    }
    
    func start() {
        coreModel.startObservation()
    }
    
    func pause() {
        coreModel.pauseObservation()
    }
    
    func stop() {
        coreModel.stopObservation()

    }
}
