//
//  StudyDetailsViewModel.swift
//  iosApp
//
//  Created by Daniil Barkov on 22.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class StudyDetailsViewModel: ObservableObject {
    private let coreModel = CoreStudyDetailsViewModel()
    @Published var studyDetailsModel: StudyDetailsModel?
    var studyStart: Int64 = 0
    var studyEnd: Int64 = 0
    
    init() {
        coreModel.onLoadStudyDetails() {
            studyDetails in
            if let studyDetails {
                self.studyDetailsModel = studyDetails
                if let start = studyDetails.study.start {
                    self.studyStart = start.epochSeconds * 1000
                }
                if let end = studyDetails.study.end {
                    self.studyEnd = end.epochSeconds * 1000
                }
            }
        }
    }
}
