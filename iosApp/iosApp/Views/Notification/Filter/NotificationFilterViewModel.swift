//
//  NotificationFilterViewModel.swift
//  More
//
//  Created by Mikolaj Luzak on 25.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Combine
import KMPNativeCoroutinesCombine
import shared

class NotificationFilterViewModel: ObservableObject {
    let coreViewModel: CoreNotificationFilterViewModel

    @Published var allFilters: [NotificationFilterTypeModel: Bool] = [:]

    private var cancellables: Set<AnyCancellable> = []

    init(coreViewModel: CoreNotificationFilterViewModel) {
        self.coreViewModel = coreViewModel
        createPublisher(for: coreViewModel.filters)
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }, receiveValue: { [weak self] filters in
            self?.allFilters = filters.mapValues {
                $0.boolValue
            }
        })
        .store(in: &cancellables)
    }

    func toggleFilters(filter: NotificationFilterTypeModel) {
        coreViewModel.toggleFilter(filter: filter)
    }
}
