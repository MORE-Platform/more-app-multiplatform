//
//  SimplequestionObservationViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import shared

import Combine
import KMPNativeCoroutinesCombine

class QuestionViewModel: ObservableObject {
    private let coreModel: QuestionCoreViewModel

    @Published var questionModel: QuestionModel?
    @Published var answers: [String] = []
    
    private var cancellables = Set<AnyCancellable>()

    init(navigationState: NavigationState) {
        coreModel = QuestionCoreViewModel(repository: AppDelegate.shared.repositories, observationFactory: AppDelegate.shared.observationFactory, scheduleId: navigationState.scheduleId, notificationId: navigationState.notificationId, observationId: navigationState.observationId)
        
        createPublisher(for: coreModel.questionModel)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: {_ in}) { [weak self] model in
                self?.questionModel = model
                self?.answers = (model?.answers as? [NSString])?.map { $0 as String } ?? []
            }
            .store(in: &cancellables)
    }

    func viewDidAppear() {
        coreModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreModel.viewDidDisappear()
    }
    
    func finish(data: AnyObject) {
        coreModel.finishQuestion(data: data)
    }
}

