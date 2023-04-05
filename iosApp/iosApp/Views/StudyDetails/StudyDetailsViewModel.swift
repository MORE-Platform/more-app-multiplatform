//
//  StudyDetailsViewModel.swift
//  iosApp
//
//  Created by Daniil Barkov on 22.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class StudyDetailsViewModel: ObservableObject {
    private let coreModel = CoreStudyDetailsViewModel()
    @Published var studyDetailsModel: StudyDetailsModel?
    
    init() {
        coreModel.onLoadStudyDetails() {
            studyDetails in
            if let studyDetails {
                self.studyDetailsModel = studyDetails
            }
        }
    }
}
