//
//  SimplequestionObservationViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class SimpleQuestionObservationViewModel: ObservableObject {
    
    private let coreModel: SimpleQuestionCoreViewModel
    private let observationFactory = IOSObservationFactory()
    
    @Published var simpleQuestoinModel: SimpleQuestionModel?
    @Published var answers: [String] = []
    @Published var answerSet: String = ""
    
    /*
    @Published var questionString: String = "Are you fine?"
    @Published var answerOptions: [String] = [
        "No",
        "Yes",
        "I don't know"
    ]*/
    
    init(scheduleId: String) {
        self.coreModel = SimpleQuestionCoreViewModel(scheduleId: scheduleId, observationFactory: observationFactory)
        
        coreModel.onLoadSimpleQuestionObservation { model in
            if let model {
                self.simpleQuestoinModel = model
                self.answers = model.answers.map { value in
                    String(describing: value)
                }
            }
        }
    }
    
    func setAnswer(answer: String) {
        self.answerSet = answer
    }
    
    func finish() {
        print("finish-----------------")
        if !self.answerSet.isEmpty {
            print(self.answerSet)
            coreModel.finishQuestion(data: self.answerSet, setObservationToDone: true)
        }
    }
}
