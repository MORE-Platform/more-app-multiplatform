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
    @EnvironmentObject var model: ScheduleViewModel
    
    var observationType: String
    var state: ScheduleState
    var start: Int64
    var end: Int64
    let action: () -> Void
    
    private let stringTable = "ScheduleListView"
    var body: some View {
        HStack {
            
            let disabled = !(Date() >= start.toDate() && Date() <= end.toDate())
            
            if observationType.lowercased() == "question-observation" {
                
                /*
                MoreActionButton(disabled: .constant(disabled), action: action) {
                    Text(String.localizedString(forKey: "start_questionnaire", inTable: stringTable, withComment: "Button to start a questionnaire"))
                }
                 */
                
                VStack(alignment: .leading) {
                    NavigationLinkButton(disabled: .constant(disabled)) {
                        QuestionObservationView()
                    } label: {
                            Text(String.localizedString(forKey: "start_questionnaire", inTable: stringTable, withComment: "Button to start a questionnaire"))
                                .foregroundColor(!disabled ? .more.white : .more.secondaryMedium)
                                .frame(maxWidth: .infinity, alignment: .center)
                    }
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
