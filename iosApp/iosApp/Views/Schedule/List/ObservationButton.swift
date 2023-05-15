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
    @EnvironmentObject var questionModalState: QuestionModalState
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
                    questionModalState.scheduleId = scheduleId
                    questionModalState.simpleQuestionOpen = true
                }) {
                    VStack {
                        Text(
                            String.localizedString(forKey: "start_questionnaire", inTable: stringTable, withComment: "Button to start a questionnaire")
                        )
                    }
                }
            } else if observationType == "lime-survey-observation" {
                MoreActionButton(disabled: .constant(disabled), action: {
                    questionModalState.scheduleId = scheduleId
                    questionModalState.limeSurveyOpen = true
                }) {
                    VStack {
                        Text(
                            "Start LimeSurvey"
                                .localize(useTable: stringTable, withComment: "Button to start a limesurvey")
                        )
                    }
                }
            } else {
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
