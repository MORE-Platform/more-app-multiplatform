//
//  ObservationErrorsViewModel.swift
//  More
//
//  Created by Jan Cortiel on 23.05.24.
//  Copyright © 2024 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

class ObservationErrorsViewModel: ObservableObject {
    @Published var observationErrors: [String] = []
    @Published var observationErrorActions: [String] = []
    
    init() {
        AppDelegate.shared.observationFactory.observationErrorsAsClosure { [weak self] errors in
            DispatchQueue.main.async {
                if let self {
                    self.observationErrors = Array(Set(errors.filterValues { $0 != Observation_.companion.ERROR_DEVICE_NOT_CONNECTED }.flatMap{$0.value}))
                    self.observationErrorActions = Array(Set(errors.filterValues { $0 == Observation_.companion.ERROR_DEVICE_NOT_CONNECTED }.flatMap{$0.value}))
                }
            }
        }
    }
}
