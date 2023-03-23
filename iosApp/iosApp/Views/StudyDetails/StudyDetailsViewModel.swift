//
//  StudyDetailsViewModel.swift
//  iosApp
//
//  Created by Daniil Barkov on 22.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class StudyDetailsViewModel: ObservableObject {
    private let coreModel: CoreStudyDetailsViewModel
    
    @Published var studyDetailsModel: StudyDetailsModel?
    @Published var totalTasks: Int64?
    @Published var completedTasks: Int64?
    
    init() {
        self.coreModel = CoreStudyDetailsViewModel()
        coreModel.loadStudy()
        coreModel.onLoadStudyDetails() {
            studyDetails in
            if let studyDetails {
                self.studyDetailsModel = studyDetails
            }
        }
        coreModel.onTotalTasks() { totalT in
            if let totalT {
                self.totalTasks = totalT.int64Value
            }
        }
        coreModel.onFinishedTasks() {
            compT in if let compT {
                self.completedTasks = compT.int64Value
            }
        }
    }
}
