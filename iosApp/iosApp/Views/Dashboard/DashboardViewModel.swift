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
    
    @Published var studyTitle: String = "Study title"
    
    @Published var study: StudySchema? = StudySchema()
    private var schedules: [ObservationSchedule] = []
    private var schedulesCount: Double = 0
    private var completedTasks: Double = 0
    
    init() {
        coreModel.onLoadStudy { study in
            if let study {
                self.study = study
                self.studyTitle = study.studyTitle
            }
        }
        self.schedulesCount = Double(self.schedules.count)
        // self.completedTasks = coreModel.getCompletedTasks()
    }
    
    func loadStudy() {
        coreModel.onLoadStudy { study in
            if let study {
                self.study = study
                self.studyTitle = study.studyTitle
            }
        }
    }
    
}
