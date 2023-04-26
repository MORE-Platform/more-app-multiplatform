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
    let coreModel: CoreDashboardFilterViewModel = CoreDashboardFilterViewModel()
    private let observationFactory: IOSObservationFactory = IOSObservationFactory()
    private let stringTable = "DashboardFilter"
    
    var delegate: DashboardFilterObserver? = nil
    
    @Published var dateFilterStringList: [String]
    @Published var observationTypes: [String]
    
    @Published var dateFilter: DateFilterModel = DateFilterModel.entireTime
    @Published var dateFilterString: String = "ENTIRE_TIME"
    @Published var observationTypeFilter: [String] = []
    
    init() {
        var list: [String] = Array(observationFactory.observationTypes())
        list.insert("All Items", at: 0)
        self.observationTypes = list
        
        self.dateFilterStringList = coreModel.getEnumAsList().map({ filter in
            String(describing: filter)
        })
        setCurrentFilters()
    }
    
    func setDateFilterValue() {
        var filterModel : DateFilterModel = DateFilterModel.todayAndTomorrow
        
        if dateFilterString == "ENTIRE_TIME" { filterModel = DateFilterModel.entireTime }
        else if dateFilterString == "TODAY_AND_TOMORROW" { filterModel = DateFilterModel.todayAndTomorrow }
        else if dateFilterString == "ONE_WEEK" { filterModel = DateFilterModel.oneWeek }
        else if dateFilterString == "ONE_MONTH" { filterModel = DateFilterModel.oneMonth }
        
        coreModel.setDateFilter(dateFilter: filterModel)
        dateFilter = filterModel
    }
    
    func setObservationTypeFilters() {
        coreModel.setTypeFilters(filters: observationTypeFilter)
    }
    
    func updateFilters(multiSelect: Bool, filter: String, list: [String], stringTable: String) -> [String] {
        return self.delegate?.onFilterChanged(multiSelect: multiSelect, filter: filter, list: list, stringTable: stringTable) ?? []
    }
    
    func updateFilterText() -> String  {
        return self.delegate?.updateFilterText() ?? ""
    }
    
    func setCurrentFilters() {
        coreModel.onLoadCurrentFilters { filters in
            self.dateFilter = filters.dateFilter
            self.dateFilterString = String(describing: self.dateFilter)
            self.observationTypeFilter = filters.typeFilter.map { value in
                String(describing: value)
            }
        }
    }
    
    func isItemSelected(selectedValuesInList: [String], option: String) -> Bool {
        if option == "All Items" {
            return selectedValuesInList.isEmpty
        } else {
            return selectedValuesInList.contains(option)
        }
    }
}
