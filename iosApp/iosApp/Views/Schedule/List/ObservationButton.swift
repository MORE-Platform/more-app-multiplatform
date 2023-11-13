//
//  StartObservationButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import SwiftUI
import shared

struct ObservationButton: View {
    @EnvironmentObject var navigationModalState: NavigationModalState
    let observationActionDelegate: ObservationActionDelegate
    var scheduleId: String
    var observationType: String
    var state: ScheduleState
    var disabled: Bool
    private let stringTable = "ScheduleListView"
    
    var body: some View {
        VStack {
            if observationType == "question-observation" {
                MoreActionButton(disabled: .constant(disabled), action: {
                    navigationModalState.openView(screen: .questionObservation, scheduleId: scheduleId)
                }) {
                    VStack {
                        Text(
                            String.localize(forKey: "start_questionnaire", withComment: "Button to start a questionnaire", inTable: stringTable)
                        )
                    }
                }
            } else if observationType == "lime-survey-observation" {
                MoreActionButton(disabled: .constant(disabled), action: {
                    navigationModalState.openView(screen: .limeSurvey, scheduleId: scheduleId)
                }) {
                    VStack {
                        Text(
                            "Start LimeSurvey"
                                .localize(withComment: "Button to start a limesurvey", useTable: stringTable)
                        )
                    }
                }
            } else if let scheduleId = navigationModalState.navigationState.scheduleId {
                MoreActionButton(disabled: .constant(disabled), action: {
                    if state == .running {
                        observationActionDelegate.pause(scheduleId: scheduleId)
                    } else {
                        observationActionDelegate.start(scheduleId: scheduleId)
                    }
                }) {
                    VStack {
                        if state == ScheduleState.running {
                            Text(
                                String.localize(forKey: "pause_observation", withComment: "Button to pause an observation", inTable: stringTable)
                            )
                        } else {
                            Text(
                                String.localize(forKey: "start_observation", withComment: "Button to start an observation", inTable: stringTable)
                            )
                        }
                    }
                }
            }
        }
    }
}
