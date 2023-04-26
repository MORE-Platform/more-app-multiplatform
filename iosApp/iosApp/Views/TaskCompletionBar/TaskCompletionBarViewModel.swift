//
//  TaskCompletionBarViewModel.swift
//  More
//
//  Created by Julia Mayrhauser on 26.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class TaskCompletionBarViewModel: ObservableObject {
    @Published var taskCompletion: TaskCompletion = TaskCompletion(finishedTasks: 0, totalTasks: 0)
    @Published var percentageOfCompletedTasks: String = "0%"
    var coreViewModel = CoreTaskCompletionBarViewModel()
    
    init() {
        self.coreViewModel.onLoadTaskCompletion { taskCompletion in
            self.taskCompletion = taskCompletion
            if self.taskCompletion.totalTasks != 0 {
                self.percentageOfCompletedTasks = String(format: "%.2f%%", (Double(taskCompletion.finishedTasks)/Double(taskCompletion.totalTasks))*100)
            }
        }
    }
}
