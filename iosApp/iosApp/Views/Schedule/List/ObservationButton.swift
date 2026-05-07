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

    var body: some View {
        VStack {
            MoreActionButton(disabled: .constant(disabled), action: buttonAction) {
                VStack {
                    if QuestionType_().matches(type: observationType) {
                        Text("start_questionnaire")
                    } else if LimeSurveyType().matches(type: observationType) {
                        Text("Start LimeSurvey")
                    } else if state == ScheduleState.running {
                        Text("pause_observation")
                    } else {
                        Text("start_observation")
                    }
                }
            }
        }
    }

    private func buttonAction() {
        var screenToOpen: NavigationScreen? =
            if QuestionType_().matches(type: observationType) {
                .questionObservation
            } else if LimeSurveyType().matches(type: observationType) {
                .limeSurvey
            } else {
                nil
            }
        Napier.event(.buttonPress, message: "\(state == .running ? "Pause" : "Start") observation \(observationType)")
        if let screenToOpen {
            navigationModalState.openView(screen: screenToOpen, scheduleId: scheduleId)
        } else if state == .running {
            observationActionDelegate.pause(scheduleId: scheduleId)
        } else {
            observationActionDelegate.start(scheduleId: scheduleId)
        }
    }
}
