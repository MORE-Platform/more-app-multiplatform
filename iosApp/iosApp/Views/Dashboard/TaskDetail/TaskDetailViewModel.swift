//
//  TaskDetailViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 17.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared
import SwiftUI

class TaskDetailViewModel: ObservableObject {
    private let coreModel: CoreDashboardViewModel = CoreDashboardViewModel()
    private let observationFactory: IOSObservationFactory
    
    @Published var observationTitle: String = "Distance Walked"
    @Published var observationType: String = "GPS"
    @Published var observationStart: String = "15.06.2023"
    @Published var observationEnd: String = "30.12.2023"
    @Published var observationTimeframe: String = "12:00 - 18:00"
    @Published var observationRepetitionInterval: String = "1x/week"
    @Published var participantInfo: String = "Any Participant Information the researcher definded beforehand"
    
    @Published var observationDates: String = "15.06.2023 - 30.12.2023"
    
    @Published var observationDatapoints: String = "10.54.773"
    
    //@Published var observation: ObservationSchema? = ObservationSchema()
    
    
    init() {
        self.observationFactory = IOSObservationFactory()
    }
    
    func loadObservation() {
            // load observation with observation core model -> julia is implementing it right now
            // save response into the variables above to use them in actual view
    }
}
