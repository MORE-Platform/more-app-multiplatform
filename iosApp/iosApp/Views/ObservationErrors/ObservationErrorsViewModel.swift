//
//  ObservationErrorsViewModel.swift
//  More
//
//  Created by Jan Cortiel on 23.05.24.
//  Copyright © 2024 Redlink GmbH. All rights reserved.
//

import Combine
import Foundation
import KMPNativeCoroutinesCombine
import shared

class ObservationErrorsViewModel: ObservableObject {
    @Published var observationErrors: [String] = []
    @Published var observationErrorActions: [String] = []

    private var cancellables = Set<AnyCancellable>()

    init() {
        createPublisher(for: ObservationStates.shared.observationErrors)
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] errors in
            self?.observationErrors = Array(Set(errors.filterValues {
                $0 != Observation_.companion.ERROR_DEVICE_NOT_CONNECTED
            }
                                                .flatMap {
                                                    $0.value
                                                }))
            self?.observationErrorActions = Array(Set(errors.filterValues {
                $0 == Observation_.companion.ERROR_DEVICE_NOT_CONNECTED
            }
                                                      .flatMap {
                                                          $0.value
                                                      }))
        }
        .store(in: &cancellables)
    }
}
