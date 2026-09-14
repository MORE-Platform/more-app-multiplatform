//
//  ObservationErrorsView.swift
//  More
//
//  Created by Jan Cortiel on 23.05.24.
//  Copyright © 2024 Redlink GmbH. All rights reserved.
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct ObservationErrorsView: View {
    @StateObject private var observationErrorsViewModel = ObservationErrorsViewModel()

    private let navigationStrings = "Navigation"
    var body: some View {
        ObservationErrorListView(taskObservationErrors: observationErrorsViewModel.observationErrors, taskObservationErrorActions: observationErrorsViewModel.observationErrorActions)
            .padding(.vertical)
            .customNavigationTitle(with: NavigationScreen.observationErrors.localize(), displayMode: .inline)
    }
}

#Preview {
    ObservationErrorsView()
}
