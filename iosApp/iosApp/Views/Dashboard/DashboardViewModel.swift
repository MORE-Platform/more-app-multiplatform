//
//  DashboardViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 02.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class DashboardViewModel: ObservableObject {
    private let coreModel: CoreDashboardViewModel = CoreDashboardViewModel()
    private let observationFactory: IOSObservationFactory
    let scheduleViewModel: ScheduleViewModel
    
    @Published var studyTitle: String = ""
    @Published var study: StudySchema? = StudySchema()
    
    private var dataJob: Ktor_ioCloseable? = nil
    
    init() {
        self.observationFactory = IOSObservationFactory()
        self.scheduleViewModel = ScheduleViewModel(observationFactory: self.observationFactory)
        
    }
    
    func loadData() {
        scheduleViewModel.loadData()
        dataJob?.close()
        dataJob = coreModel.onLoadStudy { study in
            if let study {
                self.scheduleViewModel.reinitList()
                DispatchQueue.main.async {
                    self.study = study
                    self.studyTitle = study.studyTitle
                }
            }
        }
    }

}
