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
    
    @State var dateFilter: DateFilterModel
    @State var observationTypeFilter: [String]
    
    
    init() {
                                                 
        self.observationTypeFilterList = observationFactory.self.observations.map({ ($0 as AnyObject).observationType.observationType})

        self.dateFilterList = coreModel.getEnumAsList()
        self.dateFilterStringList = coreModel.getEnumAsList().map({ filter in
            String(describing: filter)
        })
        
        self.dateFilter = DateFilterModel.todayAndTomorrow
        self.observationTypeFilter = [""]
    }
    
    func setDateFilterValue(filter: String) {
        var filterModel : DateFilterModel = DateFilterModel.todayAndTomorrow
        
        if filter == "ENTIRE_TIME" { filterModel = DateFilterModel.entireTime }
        else if filter == "TODAY_AND_TOMORROW" { filterModel = DateFilterModel.todayAndTomorrow }
        else if filter == "ONE_WEEK" { filterModel = DateFilterModel.oneWeek }
        else if filter == "ONE_MONTH" { filterModel = DateFilterModel.oneMonth }
        
        coreModel.setDateFilter(dateFilter: filterModel)
    }
}

