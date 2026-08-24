//
//  ObservationDetailsViewModel.swift
//  More
//
//  Created by Isabella Aigner on 19.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import shared

class ObservationDetailsViewModel: ObservableObject {
    private let coreModel: CoreObservationDetailsViewModel

    @Published var observationDetailModel: ObservationDetailsModel?

    init(observationId: String) {
        coreModel = CoreObservationDetailsViewModel(repository: AppDelegate.shared.repositories, observationId: observationId)
        coreModel.onLoadObservationDetails { observationDetails in
            if let observationDetails {
                self.observationDetailModel = observationDetails
            }
        }
    }

    func viewDidAppear() {
        coreModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreModel.viewDidDisappear()
    }
}
