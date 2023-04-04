//
//  StartObservationButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ObservationButton: View {
    var observationType: String
    var state: ScheduleState
    var disabled: Bool
    let action: () -> Void
    private let stringTable = "ScheduleListView"
    var body: some View {
        VStack {
            MoreActionButton(disabled: .constant(disabled), action: action) {
                VStack {
                    if state == ScheduleState.running {
                        Text(
                            String.localizedString(forKey: "pause_observation", inTable: stringTable, withComment: "Button to pause an observation")
                        )
                    } else {
                        Text(
                            String.localizedString(forKey: "start_observation", inTable: stringTable, withComment: "Button to start an observation")
                        )
                    }
                }
            }
        }
    }
}
