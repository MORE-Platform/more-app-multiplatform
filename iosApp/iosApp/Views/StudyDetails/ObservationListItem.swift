//
//  ObservationListItem.swift
//  More
//
//  Created by Julia Mayrhauser on 27.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
