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
    
    // TODO: inforopository with infodata (not yet in bakcend) - exchange mock data to backend data after it exists
    var institute: String = "Ludwig Boltzmann Institute "
    var contactPerson: String = "Dr. Markus Mustermann"
    var contactEmail: String? = "markus.mustermann@bolzmann.at"
    var contactTel: String? = nil
    
    init() {
        studyCoreModel.onLoadStudyDetails() {
            studyDetails in
            if let studyDetails {
                self.studyTitle = studyDetails.study.studyTitle
            }
        }
    }
}
