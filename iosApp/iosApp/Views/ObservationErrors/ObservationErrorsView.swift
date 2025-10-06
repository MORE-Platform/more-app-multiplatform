//
//  ObservationErrorsView.swift
//  More
//
//  Created by Jan Cortiel on 23.05.24.
//  Copyright © 2024 Redlink GmbH. All rights reserved.
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
