//
//  InfoViewModel.swift
//  More
//
//  Created by Isabella Aigner on 20.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class InfoViewModel: ObservableObject {
    
    private let studyCoreModel = CoreStudyDetailsViewModel()
    @Published var studyTitle: String?
    @Published var contactInstitute: String?
    @Published var contactPerson: String?
    @Published var contactEmail: String?
    @Published var contactPhoneNumber: String?
    
    init() {
        studyCoreModel.onLoadStudyDetails() {
            studyDetails in
            if let studyDetails {
                self.studyTitle = studyDetails.study.studyTitle
                self.contactInstitute = studyDetails.study.contactInstitute
                self.contactPerson = studyDetails.study.contactPerson
                self.contactEmail = studyDetails.study.contactEmail
                self.contactPhoneNumber = studyDetails.study.contactPhoneNumber
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
