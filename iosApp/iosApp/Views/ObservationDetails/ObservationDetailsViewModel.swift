//
//  ObservationDetailsViewModel.swift
//  More
//
//  Created by Isabella Aigner on 19.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class ObservationDetailsViewModel: ObservableObject {
    private let coreModel: CoreObservationDetailsViewModel
    
    @Published var observationDetailModel: ObservationDetailsModel?
    
    
    init(observationId: String) {
        self.coreModel = CoreObservationDetailsViewModel(observationId: observationId)
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
