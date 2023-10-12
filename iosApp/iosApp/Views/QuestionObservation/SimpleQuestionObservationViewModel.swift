//
//  SimplequestionObservationViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

protocol SimpleQuestionObservationListener {
    func onQuestionAnswered()
}

class SimpleQuestionObservationViewModel: ObservableObject {
    private let coreModel: SimpleQuestionCoreViewModel = SimpleQuestionCoreViewModel(observationFactory: AppDelegate.shared.observationFactory)
    
    @Published var simpleQuestoinModel: SimpleQuestionModel?
    @Published var answers: [String] = []
    @Published var answerSet: String = ""
    
    init() {
        coreModel.onLoadSimpleQuestionObservation { model in
            if let model {
                self.simpleQuestoinModel = model
                self.answers = model.answers.map { value in
                    String(describing: value)
                }
            }
        }
    }
    
    func setScheduleId(navigationState: NavigationState) {
        if let scheduleId = navigationState.scheduleId {
            coreModel.setScheduleId(scheduleId: scheduleId, notificationId: navigationState.notificationId)
        } else if let observationId = navigationState.observationId {
            coreModel.setScheduleViaObservationId(observationId: observationId, notificationId: navigationState.notificationId)
        }
    }
    
    func viewDidAppear() {
        coreModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreModel.viewDidDisappear()
        answerSet = ""
    }
    
    func setAnswer(answer: String) {
        self.answerSet = answer
    }
    
    func finish() {
        if !self.answerSet.isEmpty {
            coreModel.finishQuestion(data: self.answerSet, setObservationToDone: true)
        }
    }

}
