//
//  TaskCompletionBarViewModel.swift
//  More
//
//  Created by Julia Mayrhauser on 26.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Combine
import KMPNativeCoroutinesCombine
import shared

class TaskCompletionBarViewModel: ObservableObject {
    @Published var taskCompletion: TaskCompletion = TaskCompletion(finishedTasks: 0, totalTasks: 0)
    @Published var taskCompletionPercentage: Double = 0
    var coreViewModel = CoreTaskCompletionBarViewModel(repository: AppDelegate.shared.repositories, dispatcher: AppDispatchers.shared.default_)

    private var cancellables: Set<AnyCancellable> = []

    init() {
        createPublisher(for: coreViewModel.taskCompletion)
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] completion in
            self?.taskCompletion = completion
            self?.taskCompletionPercentage = (Double(completion.finishedTasks) / Double(completion.totalTasks)) * 100
        }
        .store(in: &cancellables)
    }
}
