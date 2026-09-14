//
//  ObservationErrorListView.swift
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

import shared
import SwiftUI

struct ObservationErrorListView: View {
    let taskObservationErrors: [String]
    let taskObservationErrorActions: [String]

    @State private var scrollViewContentSize: CGSize = .zero

    var body: some View {
        if !taskObservationErrors.isEmpty || !taskObservationErrorActions.isEmpty {
            VStack {
                if !taskObservationErrors.isEmpty {
                    ScrollView {
                        VStack {
                            ForEach(taskObservationErrors, id: \.self) { error in
                                HStack {
                                    Image(systemName: "exclamationmark.triangle")
                                        .font(.more.headline)
                                        .foregroundColor(.more.important)
                                        .padding(.trailing, 4)
                                    BasicText(text: "\(error)!")
                                }
                                .padding(.bottom)
                            }
                        }
                    }
                    .frame(maxHeight: 100)
                }

                if !taskObservationErrorActions.isEmpty {
                    if taskObservationErrorActions
                        .contains(Observation_.companion.ERROR_DEVICE_NOT_CONNECTED) {
                        MoreActionButton(disabled: .constant(false)) {
                            ViewManager.shared.showBLEView(state: true)
                        } label: {
                            HStack {
                                Image(systemName: "applewatch")
                                    .foregroundColor(.more.white)
                                    .padding(.trailing, 4)
                                Text("Devices")
                            }
                        }
                    }
                }
            }
        }
    }
}

#Preview {
    ObservationErrorListView(taskObservationErrors: ["Error"], taskObservationErrorActions: [Observation_.companion.ERROR_DEVICE_NOT_CONNECTED])
}
