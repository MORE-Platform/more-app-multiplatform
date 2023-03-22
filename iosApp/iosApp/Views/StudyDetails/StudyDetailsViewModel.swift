//
//  StudyDetailsViewModel.swift
//  iosApp
//
//  Created by Daniil Barkov on 22.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class StudyDetailsViewModel: ObservableObject {
    private let coreModel: CoreDashboardViewModel = CoreDashboardViewModel()
    
    @Published var studyTitle: String = ""
    @Published var study: StudySchema? = StudySchema()
    
    init() {
        self.loadStudy()
    }
    
    func loadStudy() {
        coreModel.onLoadStudy { study in
            if let study {
                DispatchQueue.main.async {
                    self.study = study
                    self.studyTitle = study.studyTitle
                }
            }
        }
    }
}
