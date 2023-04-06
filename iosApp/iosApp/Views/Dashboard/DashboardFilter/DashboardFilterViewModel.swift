//
//  DashboardFilterViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared
import RealmSwift

class DashboardFilterViewModel: ObservableObject {
    let coreModel: CoreDashboardFilterViewModel = CoreDashboardFilterViewModel()
    private let observationFactory: IOSObservationFactory = IOSObservationFactory()
    
    @Published var dateFilterList: [DateFilterModel]
    @Published var dateFilterStringList: [String]
    @Published var observationTypeFilterList: [String]
    
    @State var dateFilter: DateFilterModel = DateFilterModel.entireTime
    @State var observationTypeFilter: [String] = ["All Items"]
    
    @State var currentFilter: FilterModel? = nil
    
    
    init() {
        var list: [String] = observationFactory.self.observations.map({ ($0 as AnyObject).observationType.observationType})
        list.insert("All Items", at: 0)
        self.observationTypeFilterList = list
        
        self.dateFilterList = coreModel.getEnumAsList()
        self.dateFilterStringList = coreModel.getEnumAsList().map({ filter in
            String(describing: filter)
        })
        
        setCurrentFilters()
    }
    
    func setDateFilterValue(filter: String) {
        var filterModel : DateFilterModel = DateFilterModel.todayAndTomorrow
        
        if filter == "ENTIRE_TIME" { filterModel = DateFilterModel.entireTime }
        else if filter == "TODAY_AND_TOMORROW" { filterModel = DateFilterModel.todayAndTomorrow }
        else if filter == "ONE_WEEK" { filterModel = DateFilterModel.oneWeek }
        else if filter == "ONE_MONTH" { filterModel = DateFilterModel.oneMonth }
        
        coreModel.setDateFilter(dateFilter: filterModel)
        self.setCurrentFilters()
    }
    
    func setCurrentFilters() {
        coreModel.onLoadCurrentFilters { filters in
            self.dateFilter = filters.dateFilter
            self.observationTypeFilter = filters.typeFilter.map { value in
                String(describing: value)
            }
            
            if self.observationTypeFilter.isEmpty {
                self.observationTypeFilter = ["All Items"]
            }
        }
    }
}

