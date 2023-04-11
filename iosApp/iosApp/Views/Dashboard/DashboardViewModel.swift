//
//  DashboardViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 02.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared
import SwiftUI

class DashboardViewModel: ObservableObject {
    private let coreModel: CoreDashboardViewModel = CoreDashboardViewModel()
    private let observationFactory: IOSObservationFactory
    let scheduleViewModel: ScheduleViewModel
    
    @Published var studyTitle: String = ""
    @Published var study: StudySchema? = StudySchema()
    
    init(dashboardFilterViewModel: DashboardFilterViewModel) {
        self.observationFactory = IOSObservationFactory()
        self.scheduleViewModel = ScheduleViewModel(observationFactory: self.observationFactory, dashboardFilterViewModel: dashboardFilterViewModel)
        coreModel.onLoadStudy { study in
            if let study {
                self.study = study
                self.studyTitle = study.studyTitle
            }
        }
    }

}
