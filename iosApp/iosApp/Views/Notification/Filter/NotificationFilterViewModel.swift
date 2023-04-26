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
    
    @Published var allFilters: [String] = []
    @Published var currentFilters: [String] = []
    
    init() {
        allFilters = coreModel.getEnumAsList().map({ filter in
            String(describing: filter)
        })
        loadCurrentFilters()
    }
    
    func updateFilters(multiselect: Bool = true, filter: String, list: [String], stringTable: String) ->
    [String] {
        return self.delegate?.onFilterChanged(filter: filter, list: list, stringTable: stringTable)
        ?? []
    }
    
    func loadCurrentFilters() {
        coreModel.onLoadCurrentFilters { filters in
            self.currentFilters = filters.map { value in
                String(describing: value)
            }
        }
    }
    
    func processFilterChange(filter: String) {
        coreModel.processFilterChange(filter: filter)
    }
    
    func isItemSelected(selectedValues: [String], option: String) -> Bool {
        if option == NotificationFilterTypeModel.all.type {
            return selectedValues.isEmpty
        }
        return selectedValues.contains(option)
    }
}
