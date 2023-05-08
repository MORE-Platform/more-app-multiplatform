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
    let coreModel: CoreNotificationFilterViewModel = CoreNotificationFilterViewModel()
 
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
        var selectedValueList = list
        if filter == String(describing: NotificationFilterTypeModel.all) {
            selectedValueList.removeAll()
        } else if selectedValueList.contains(filter) {
            selectedValueList.remove(at: selectedValueList.firstIndex(of: filter)!)
        } else {
            selectedValueList.append(filter)
        }
        processFilterChange(filter: filter)
        return selectedValueList
    }
    
    
    func isItemSelected(selectedValues: [String], option: String) -> Bool {
        if option == String(describing: NotificationFilterTypeModel.all) {
            return selectedValues.isEmpty
        }
        return selectedValues.contains(option)
    }
    
    func getFilterText() -> String {
        if(currentFilters.isEmpty) {
            return String.localizedString(forKey: "ALL", inTable: stringTable, withComment: "Get NotificationFilter String")
        } else {
            return currentFilters.map{
                String.localizedString(forKey: String($0), inTable: stringTable, withComment: "Get NotificationFilter String")
            }.joined(separator: ", ")
        }
    }
}
