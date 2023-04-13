//
//  DashboardFilterViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

class DashboardFilterViewModel: ObservableObject {
    let coreModel: CoreDashboardFilterViewModel = CoreDashboardFilterViewModel()
    private let observationFactory: IOSObservationFactory = IOSObservationFactory()
    private let stringTable = "DashboardFilter"
    
    @Published var dateFilterStringList: [String]
    @Published var observationTypes: [String]
    
    @Published var dateFilter: DateFilterModel = DateFilterModel.entireTime
    @Published var dateFilterString: String = "ENTIRE_TIME"
    @Published var observationTypeFilter: [String] = []
    
    @Published var currentFilter: FilterModel? = nil
    
    
    init() {
        var list: [String] = observationFactory.self.observations.map({ ($0 as AnyObject).observationType.observationType})
        list.insert(String.localizedString(forKey: "All Items", inTable: stringTable, withComment: "String for all items"), at: 0)
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
    }
    
    func setObservationTypeFilters() {
        coreModel.setTypeFilters(filters: observationTypeFilter)
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
}

