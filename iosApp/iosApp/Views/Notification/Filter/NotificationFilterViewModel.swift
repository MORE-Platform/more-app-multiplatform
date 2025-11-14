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

import shared

class NotificationFilterViewModel: ObservableObject {
    private let stringTable = "NotificationFilter"
    let coreViewModel: CoreNotificationFilterViewModel
 
    @Published var allFilters: [NotificationFilterTypeModel: KotlinBoolean] = [:]
    
    init(coreViewModel: CoreNotificationFilterViewModel) {
        self.coreViewModel = coreViewModel
        coreViewModel.onFilterChange { filters in
            self.allFilters = filters
        }
    }
    
    func viewDidAppear() {
        coreViewModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }
    
    
    func toggleFilters(filter: NotificationFilterTypeModel) {
        coreViewModel.toggleFilter(filter: filter)
    }
}
