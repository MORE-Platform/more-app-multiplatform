//
//  DashboardFilterViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

protocol DashboardFilterObserver {
    func onFilterChanged(multiSelect: Bool, filter: String, list: [String], stringTable: String) -> [String]
    func updateFilterText() -> String
}

class DashboardFilterViewModel: ObservableObject {
    let coreViewModel: CoreDashboardFilterViewModel = CoreDashboardFilterViewModel()
    private let stringTable = "DashboardFilter"
    
    var delegate: DashboardFilterObserver? = nil
    
    @Published var currentTypeFilter: [String: KotlinBoolean] = [:]
    @Published var typeFilterActive = false
    
    @Published var currentDateFilter: [DateFilter: KotlinBoolean] = [:]
    
    init() {
        coreViewModel.addTypes(observationFactory: AppDelegate.observationFactory)
        coreViewModel.onNewDateFilter { [weak self] dateFilter in
            self?.currentDateFilter = dateFilter
        }
        
        coreViewModel.onNewTypeFilter { [weak self] typeFilter in
            if let self {
                self.currentTypeFilter = typeFilter
                self.typeFilterActive = !self.coreViewModel.activeTypeFilter()
            }
        }
    }
    
    func toggleTypeFilter(type: String) {
        coreViewModel.toggleTypeFilter(type: type)
    }
    
    func clearTypeFilter() {
        coreViewModel.clearTypeFilters()
    }
    
    func toggleDateFilter(dateFilter: DateFilter) {
        coreViewModel.toggleDateFilter(date: dateFilter)
    }
    
    func updateFilterText() -> String  {
        return self.delegate?.updateFilterText() ?? ""
    }
    
    func isItemSelected(selectedValuesInList: [String], option: String) -> Bool {
        var isSelected = false
        let allItemsString = String.localizedString(forKey: "All Items", inTable: stringTable, withComment: "String for All Items")
        if option == allItemsString && selectedValuesInList.isEmpty {
            isSelected = true
        } else {
            isSelected = selectedValuesInList.contains(option)
        }
        return isSelected
    }
}
