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
    var studyStart: Date = Date()
    var studyEnd: Date = Date()
    
    init() {
        coreModel.onLoadStudyDetails() {[weak self] studyDetails in
            if let self, let studyDetails {
                self.studyDetailsModel = studyDetails
                if let start = studyDetails.study.start {
                    self.studyStart = start.epochSeconds.toDate()
                }
                if let end = studyDetails.study.end {
                    self.studyEnd = end.epochSeconds.toDate()
                }
            }
        }
    }
    
    func viewDidAppear() {
        coreModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreModel.viewDidDisappear()
        studyDetailsModel = nil
    }
}
