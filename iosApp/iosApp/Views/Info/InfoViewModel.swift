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
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import Combine
import KMPNativeCoroutinesCombine
import shared

class InfoViewModel: ObservableObject {
    let studyCoreModel = CoreStudyDetailsViewModel(shared: AppDelegate.shared, customViewIdentifier: NavigationRoute.info.viewIdentifier)
    @Published var studyTitle: String?
    @Published var contactInstitute: String?
    @Published var contactPerson: String?
    @Published var contactEmail: String?
    @Published var contactPhoneNumber: String?
    @Published var participantId: Int?
    @Published var participantAlias: String?

    private var cancellables = Set<AnyCancellable>()

    init() {
        createPublisher(for: studyCoreModel.studyModel)
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] studyDetails in
            if let self, let studyDetails {
                self.studyTitle = studyDetails.study.studyTitle
                self.contactInstitute = studyDetails.study.contactInstitute
                self.contactPerson = studyDetails.study.contactPerson
                self.contactEmail = studyDetails.study.contactEmail
                self.contactPhoneNumber = studyDetails.study.contactPhoneNumber
                self.participantId = studyDetails.study.participantId?.intValue
                self.participantAlias = studyDetails.study.participantAlias
            }
        }
        .store(in: &cancellables)
    }
}
