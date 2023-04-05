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
    private let dashboardStringTable: String = "DashboardFilter"
    
    //@Published var typeFilterList: [String] = IOSObservationFactory().observations.map {String(($0 as AnyObject).observationType)}
    @Published var typeFilterList: [String]
    @Published var dateFilterList: [DateFilterModel]
    
    //let dateFilterOptions: Set<String> = ["Today and Tomorrow", "1 week", "1 month", "Entire Time"]
    //let observationTypeFilterOptions: Set<String> = ["All Items", "Polar Varity Sensor", "GPS Mobile Sensor", "Accelerometer Mobile", "Question Observation", "Lime Survey"]
    
    @State var dateFilter: DateFilterModel
    @State var observationTypeFilter: [IOSObservationFactory]
    
    init() {
        self.observationFactory = IOSObservationFactory()
        
        self.typeFilterList = observationFactory.observations.map({ observation in
            return (observation as AnyObject).observationType
        })

        
        self.dateFilterList = coreModel.getEnumAsList()
        self.dateFilter = DateFilterModel.entireTime
        self.observationTypeFilter = []
    }
    
    /*func getDateFilterList(): [String] {
     
         
        return []
    }*/
    
    func getDateEnumString(filter: DateFilterModel) -> String {
        return filter.name
    }
}

