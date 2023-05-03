//
//  SimpleQuestionModalStateViewModel.swift
//  More
//
//  Created by Isabella Aigner on 02.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

class SimpleQuestionModalStateViewModel: ObservableObject {
    @Published var isQuestionOpen: Bool
    @Published var isQuestionThankYouOpen: Bool
    
    init(isQuestionOpen: Bool?, isQuestionThankYouOpen: Bool?) {
        self.isQuestionOpen = isQuestionOpen ?? false
        self.isQuestionThankYouOpen = isQuestionThankYouOpen ?? false
    }
}
