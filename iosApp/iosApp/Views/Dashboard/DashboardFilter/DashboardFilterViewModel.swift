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
    let coreViewModel: CoreDashboardFilterViewModel = CoreDashboardFilterViewModel(repository: AppDelegate.shared.repositories)

    var delegate: DashboardFilterObserver?

    @Published var currentTypeFilter: [String: KotlinBoolean] = [:]
    @Published var typeFilterActive = false

    @Published var currentDateFilter: [DateFilter: KotlinBoolean] = [:]

    init() {
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

    func viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreViewModel.viewDidDisappear()
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

    func updateFilterText() -> String {
        return delegate?.updateFilterText() ?? ""
    }

    func isItemSelected(selectedValuesInList: [String], option: String) -> Bool {
        var isSelected = false
        let allItemsString = String(localized: "All Items")
        if option == allItemsString && selectedValuesInList.isEmpty {
            isSelected = true
        } else {
            isSelected = selectedValuesInList.contains(option)
        }
        return isSelected
    }
}
