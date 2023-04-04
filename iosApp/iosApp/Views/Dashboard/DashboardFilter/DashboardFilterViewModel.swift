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
    //let dateFilterModel: DateFilterModel = DateFilterModel()
    //private let dateFilterModel: DateFilterModel = DateFilterModel()
    //private let filterModel: FilterModel = FilterModel()
    
    //private let dateFilterModel: DateFilterModel
    //private let filterModel: FilterModel
    
    let dateFilterOptions: Set<String> = ["Today and Tomorrow", "1 week", "1 month", "Entire Time"]
    let observationTypeFilterOptions: Set<String> = ["All Items", "Polar Varity Sensor", "GPS Mobile Sensor", "Accelerometer Mobile", "Question Observation", "Lime Survey"]
    
    //let dateFilterOptions = DateFilterModel.values
    
    @Published var dateFilter: String
    @Published var observationTypeFilter: Set<String> = ["GPS Mobile Sensor", "Question Observation"]
    
    init() {
        //self.dateFilter = coreModel.currentFilter.dateFilter ?? DateFilterModel(name: Date().month, ordinal: Int32)
        //self.observationTypeFilter = coreModel.currentFilter.observationTypeFilter as! Set<String>
        self.dateFilter = "Entire Time"
    }
}

