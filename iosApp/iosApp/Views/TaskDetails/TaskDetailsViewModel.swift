//
//  TaskDetailsViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class TaskDetailsViewModel: ObservableObject {
    private let coreScheduleViewModel: CoreScheduleViewModel
    private let coreModel: CoreTaskDetailsViewModel
    
    @Published var taskDetailsModel: TaskDetailsModel?
    
    init(coreScheduleViewModel: CoreScheduleViewModel, observationId: String, scheduleId: String) {
        self.coreScheduleViewModel = coreScheduleViewModel
        self.coreModel = CoreTaskDetailsViewModel(coreScheduleViewModel: coreScheduleViewModel)
        coreModel.onLoadTaskDetails(observationId: observationId, scheduleId: scheduleId) { taskDetails in
            if let taskDetails {
                self.taskDetailsModel = taskDetails
            }
        }
        print(taskDetailsModel?.observationTitle ?? "")
    }
}
