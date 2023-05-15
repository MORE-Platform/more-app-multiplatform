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
    private var coreModel: SimpleQuestionCoreViewModel?
    
    var delegate: SimpleQuestionObservationListener? = nil
    
    @Published var simpleQuestoinModel: SimpleQuestionModel?
    @Published var answers: [String] = []
    @Published var answerSet: String = ""
    
    //@Published var showThankYouScreen = false
    
    func viewDidAppear(scheduleId: String) {
        self.coreModel = SimpleQuestionCoreViewModel(scheduleId: scheduleId, observationFactory: AppDelegate.observationFactory)
        
        if let coreModel = self.coreModel {
            coreModel.onLoadSimpleQuestionObservation { model in
                if let model {
                    self.simpleQuestoinModel = model
                    self.answers = model.answers.map { value in
                        String(describing: value)
                    }
                }
            }
        }
    }
    
    func setAnswer(answer: String) {
        self.answerSet = answer
    }
    
    func finish() {
        if !self.answerSet.isEmpty {
            if let coreModel = self.coreModel {
                coreModel.finishQuestion(data: self.answerSet, setObservationToDone: true)
            }
        }
    }

}
