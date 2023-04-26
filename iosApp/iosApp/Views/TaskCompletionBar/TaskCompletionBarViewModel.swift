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
    @Published var taskCompletionPercentage: Double = 0
    var coreViewModel = CoreTaskCompletionBarViewModel()
    
    init() {
        loadTaskCompletion()
    }
    
    func loadTaskCompletion() {
        self.coreViewModel.onLoadTaskCompletion { taskCompletion in
            self.taskCompletion = taskCompletion
            if taskCompletion.totalTasks != 0 {
                self.taskCompletionPercentage = (Double(taskCompletion.finishedTasks)/Double(taskCompletion.totalTasks)) * 100
            }
        }
    }
}
