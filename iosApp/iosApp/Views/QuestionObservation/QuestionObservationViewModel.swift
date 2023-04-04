//
//  QuestionObservationViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class QuestionObservationViewModel: ObservableObject {
    @Published var questionString: String = "Are you fine?"
    @Published var answerOptions: [String] = [
        "No",
        "Yes",
        "I don't know"
    ]
    
    init() {
        self.questionString = questionString
        self.answerOptions = answerOptions
    }
}
