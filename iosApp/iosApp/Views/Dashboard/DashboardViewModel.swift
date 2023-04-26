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
    let scheduleViewModel: ScheduleViewModel
    
    @Published var studyTitle: String = ""
    @Published var study: StudySchema? = StudySchema()
    @Published var filterText: String = ""
    
    init(scheduleViewModel: ScheduleViewModel) {
        self.scheduleViewModel = scheduleViewModel
        coreModel.onLoadStudy { study in
            if let study {
                self.study = study
                self.studyTitle = study.studyTitle
            }
        }
        self.filterText = String.localizedString(forKey: "no_filter_activated", inTable: "DashboardFilter", withComment: "String for no filter set")
    }
    
    func updateBluetoothDevices() {
        BluetoothDeviceRepository(bluetoothConnector: IOSBluetoothConnector()).updateConnectedDevices()
    }
}
