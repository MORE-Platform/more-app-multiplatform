//
//  NotificationFilterViewModel.swift
//  More
//
//  Created by Mikolaj Luzak on 25.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
