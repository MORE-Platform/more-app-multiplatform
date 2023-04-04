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
    @State var observationType: String
    @Binding var state: ScheduleState
    @State var disabled: Bool
    let action: () -> Void
    private let stringTable = "ScheduleListView"
    var body: some View {
        HStack {
            if observationType.lowercased() == "question-observation" {
                MoreActionButton(disabled: $disabled, action: action) {
                    Text(String.localizedString(forKey: "start_questionnaire", inTable: stringTable, withComment: "Button to start a questionnaire"))
                }
            } else {
                MoreActionButton(disabled: $disabled, action: action) {
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
}
