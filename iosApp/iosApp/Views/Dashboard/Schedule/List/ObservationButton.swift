//
//  StartObservationButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct ObservationButton: View {
    @EnvironmentObject var simpleQuestionModalStateVM: SimpleQuestionModalStateViewModel
    @ObservedObject var simpleQuestionViewModel: SimpleQuestionObservationViewModel
    var scheduleId: String
    var observationType: String
    var state: ScheduleState
    var disabled: Bool
    let action: () -> Void
    private let stringTable = "ScheduleListView"
    
    
    
    var body: some View {
        VStack {
            if observationType == "question-observation" {
                
                MoreActionButton(disabled: .constant(disabled), action: action) {
                    VStack {
                        Text(
                            String.localizedString(forKey: "start_questionnaire", inTable: stringTable, withComment: "Button to start a questionnaire")
                        )
                    }
                } .sheet(isPresented: $simpleQuestionModalStateVM.isQuestionOpen) {
                    SimpleQuetionObservationView(viewModel: simpleQuestionViewModel, scheduleId: scheduleId).environmentObject(simpleQuestionModalStateVM)
                }
            } else {
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
}
