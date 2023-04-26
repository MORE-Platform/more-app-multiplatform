//
//  NotificationFilterViewModel.swift
//  More
//
//  Created by Mikolaj Luzak on 25.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

protocol NotificationFilterObserver {
    func onFilterChanged(filter: String, list: [String], stringTable: String) -> [String]
}

class NotificationFilterViewModel: ObservableObject {
    let coreModel: CoreNotificationFilterViewModel = CoreNotificationFilterViewModel()
    private let stringTable = "NotificationFilter"
    
    var delegate: NotificationFilterObserver? = nil
 
    @Published var filterText: String = "notificationFilterText"
    @Published var allFilters: [String] = []
    @Published var currentFilters: [String] = []
    
    init() {
        self.allFilters = coreModel.getEnumAsList().map({ filter in
            String(describing: filter)
        })
        loadCurrentFilters()
    }
    
    func processFilterChange(filter: String) {
        coreModel.processFilterChange(filter: filter.lowercased())
    }
    
    func loadCurrentFilters() {
        coreModel.onLoadCurrentFilters { filters in
            self.currentFilters = filters.map { value in
                String(describing: value)
            }
        }
    }
    
    func updateFilters(multiselect: Bool = true, filter: String, list: [String], stringTable: String) ->
    [String] {
        return self.delegate?.onFilterChanged(filter: filter, list: list, stringTable: stringTable)
        ?? []
    }
    
    func isItemSelected(selectedValues: [String], option: String) -> Bool {
        if option == String(describing: NotificationFilterTypeModel.all) {
            return selectedValues.isEmpty
        }
        return selectedValues.contains(option)
    }
}
