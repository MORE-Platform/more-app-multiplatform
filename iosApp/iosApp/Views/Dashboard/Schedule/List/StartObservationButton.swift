//
//  StartObservationButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct StartObservationButton: View {
    var observationType: String
    private let stringTable = "ScheduleListView"
    var body: some View {
        HStack {
            if observationType.lowercased() == "question-observation" {
                MoreActionButton(action: {}) {
                    Text(String.localizedString(forKey: "start_questionnaire", inTable: stringTable, withComment: "Button to start a questionnaire"))
                }
            } else {
                MoreActionButton(action: {}) {
                    Text(String.localizedString(forKey: "start_observation", inTable: stringTable, withComment: "Button to start an observation"))
                }
            }
        }
    }
}
