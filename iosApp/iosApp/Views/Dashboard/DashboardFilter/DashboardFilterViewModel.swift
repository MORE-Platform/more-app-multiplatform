//
//  DashboardFilterViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class DashboardFilterViewModel: ObservableObject {
    //private let coreModel: CoreDashboardFilterModel = CoreDashboardFilterMOdel()
    
    @Published var dateFilterOptions: Set<String> = ["Today", "Today and Tomorrow", "1 week", "1 month", "Entire Time"]
    @Published var observationTypeFilterOptions: Set<String> = ["All Items", "Polar Varity Sensor", "GPS Mobile Sensor", "Accelerometer Mobile", "Question Observation", "Lime Survey"]
    
    @Published var dateFilter: String = "Today"
    @Published var observationTypeFilter: Set<String> = ["GPS Mobile Sensor", "Question Observation"]
    /*
    init(dateFilter?: String, observationTypeFilter?: Set<String>) {
        self.dateFilter = dateFilter ? "Today"
        self.observationTypeFilter = observationTypeFilter ? "All Items"
    }*/
    
    //hasObservationTypeFilter()
    
    func setDateFilter(dateFilter: String) {}
    func resetDateFilter() {}
    
    //func setObservationTypeFilter(observationTypeFilter: Set<String>)
    func resetObservationTypeFilter() {}
}
