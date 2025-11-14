//
//  InfoViewModel.swift
//  More
//
//  Created by Isabella Aigner on 20.04.23.
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

class InfoViewModel: ObservableObject {
    
    private let studyCoreModel = CoreStudyDetailsViewModel()
    @Published var studyTitle: String?
    @Published var contactInstitute: String?
    @Published var contactPerson: String?
    @Published var contactEmail: String?
    @Published var contactPhoneNumber: String?
    @Published var participantId: Int?
    @Published var participantAlias: String?
    
    init() {
        studyCoreModel.onLoadStudyDetails() {
            studyDetails in
            if let studyDetails {
                self.studyTitle = studyDetails.study.studyTitle
                self.contactInstitute = studyDetails.study.contactInstitute
                self.contactPerson = studyDetails.study.contactPerson
                self.contactEmail = studyDetails.study.contactEmail
                self.contactPhoneNumber = studyDetails.study.contactPhoneNumber
                self.participantId = studyDetails.study.participantId?.intValue
                self.participantAlias = studyDetails.study.participantAlias
            }
        }
    }
    
    func viewDidAppear() {
        studyCoreModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        studyCoreModel.viewDidDisappear()
    }
}
