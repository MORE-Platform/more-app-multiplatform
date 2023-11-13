//
//  StudyDetailsViewModel.swift
//  iosApp
//
//  Created by Daniil Barkov on 22.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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
