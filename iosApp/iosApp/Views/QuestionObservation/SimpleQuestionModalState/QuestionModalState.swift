//
//  SimpleQuestionModalStateViewModel.swift
//  More
//
//  Created by Isabella Aigner on 02.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

class QuestionModalState: ObservableObject {
    @Published var simpleQuestionOpen = false
    @Published var simpleQuestionThankYouOpen = false
    @Published var limeSurveyOpen = false
    
    @Published var scheduleId: String = ""
}
