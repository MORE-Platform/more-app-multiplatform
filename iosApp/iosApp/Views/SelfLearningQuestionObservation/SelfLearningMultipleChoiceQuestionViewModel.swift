//
//  SelfLearningMultipleChoiceQuestionViewModel.swift
//  More
//
//  Created by dhp lbi on 22.09.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

protocol SelfLearningMultipleChoiceQuestionListener {
    func onQuestionAnswered()
}

class SelfLearningMultipleChoiceQuestionViewModel: ObservableObject {
    private let coreModel: SelfLearningMultipleChoiceQuestionCoreViewModel = SelfLearningMultipleChoiceQuestionCoreViewModel(observationFactory: AppDelegate.shared.observationFactory, sharedStorageRepository: AppDelegate.shared.sharedStorageRepository)
    
    @Published var selfLearningMultipleChoiceQuestionModel: SelfLearningMultipleChoiceQuestionModel?
    @Published var answers: [String] = []
    @Published var answerSet: [String] = []
    @Published var userTextAnswer: String = ""
    
    init() {
        coreModel.onLoadSelfLearningMultipleChoiceQuestionObservation { model in
            if let model {
                self.selfLearningMultipleChoiceQuestionModel = model
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
        answerSet = []
        userTextAnswer = ""
    }
    
    func setAnswer(answer: [String]) {
        self.answerSet.removeAll()
        self.answerSet = answer
    }
    
    func finish() {
        if !self.answerSet.isEmpty {
            coreModel.finishQuestion(data: self.answerSet, userTextAnswer: self.userTextAnswer, setObservationToDone: true)
        }
    }

}
