//
//  ObservationListItem.swift
//  More
//
//  Created by Julia Mayrhauser on 27.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI
import shared

struct ObservationListItem: View {
    var observation: ObservationSchema
    var body: some View {
        VStack {
            NavigationLink {
                ObservationDetailsView(viewModel: ObservationDetailsViewModel(observationId: observation.observationId))
            } label: {
                ModuleListItem(observation: observation).padding(.bottom)
            }
        }
    }
}
