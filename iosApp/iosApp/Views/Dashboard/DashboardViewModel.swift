//
//  DashboardViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 02.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class DashboardViewModel: ObservableObject {
    private let coreModel: CoreDashboardViewModel = CoreDashboardViewModel()
    private let observationFactory: IOSObservationFactory
    let scheduleViewModel: ScheduleViewModel
    
    @Published var studyTitle: String = ""
    @Published var study: StudySchema? = StudySchema()
    @Published var filterText: String = ""
    
    let bleConnector = IOSBluetoothConnector()
    
    init(dashboardFilterViewModel: DashboardFilterViewModel) {
        self.observationFactory = IOSObservationFactory()
        self.scheduleViewModel = ScheduleViewModel(observationFactory: self.observationFactory, dashboardFilterViewModel: dashboardFilterViewModel)
        bleConnector.delegate = self
        coreModel.onLoadStudy { study in
            if let study {
                self.study = study
                self.studyTitle = study.studyTitle
            }
        }
        self.filterText = String.localizedString(forKey: "no_filter_activated", inTable: "DashboardFilter", withComment: "String for no filter set")
    }

}

extension DashboardViewModel: BLEConnectorDelegate {
    func bleHasPower() {
        self.bleConnector.scan()
    }
}
