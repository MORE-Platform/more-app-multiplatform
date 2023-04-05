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
    private let observationFactory: IOSObservationFactory
    
    @Published var dateFilterList: [DateFilterModel]
    @Published var dateFilterStringList: [String]
    @State var dateFilter: DateFilterModel
    
    @Published var typeFilterList: [ObservationType]
    @State var observationTypeFilter: [String]
    
    init() {
        self.observationFactory = IOSObservationFactory()
        
        self.typeFilterList = []

        self.dateFilterList = coreModel.getEnumAsList()
        self.dateFilterStringList = coreModel.getEnumAsList().map({ filter in
            String(describing: filter)
        })
        
        self.dateFilter = DateFilterModel.todayAndTomorrow
        self.observationTypeFilter = [""]
    }
    
    func getDateFilterValue(filter: String) -> DateFilterModel {
        var filterModel : DateFilterModel = DateFilterModel.todayAndTomorrow
        
        if filter == "ENTIRE_TIME" { filterModel = DateFilterModel.entireTime }
        else if filter == "TODAY_AND_TOMORROW" { filterModel = DateFilterModel.todayAndTomorrow }
        else if filter == "ONE_WEEK" { filterModel = DateFilterModel.oneWeek }
        else if filter == "ONE_MONTH" { filterModel = DateFilterModel.oneMonth }
        
        return filterModel
    }
}

