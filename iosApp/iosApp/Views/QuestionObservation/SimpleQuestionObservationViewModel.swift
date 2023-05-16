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
    private let coreModel: SimpleQuestionCoreViewModel = SimpleQuestionCoreViewModel(observationFactory: AppDelegate.observationFactory)
    
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
    
    func setScheduleId(scheduleId: String) {
        coreModel.setScheduleId(scheduleId: scheduleId)
    }
    
    func viewDidAppear() {
        coreModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreModel.viewDidDisappear()
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
