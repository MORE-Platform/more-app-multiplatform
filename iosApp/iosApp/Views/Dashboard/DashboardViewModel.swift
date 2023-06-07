//
//  DashboardViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 02.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class DashboardViewModel: ObservableObject {
    private let coreViewModel: CoreDashboardViewModel = CoreDashboardViewModel()
    let scheduleViewModel: ScheduleViewModel
    
    @Published var studyTitle: String = ""
    @Published var study: StudySchema? = StudySchema()
    @Published var filterText: String = ""
    
    init(scheduleViewModel: ScheduleViewModel) {
        self.scheduleViewModel = scheduleViewModel
        coreViewModel.onLoadStudy { study in
            if let study {
                self.study = study
                self.studyTitle = study.studyTitle
            }
        }
        self.filterText = String.localize(forKey: "no_filter_activated", withComment: "String for no filter set", inTable: "DashboardFilter")
    }
    
    func viewDidAppear() {
        coreViewModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }
}
