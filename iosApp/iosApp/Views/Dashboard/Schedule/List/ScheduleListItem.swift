//
//  ScheduleListItem.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared
import RealmSwift
import Foundation

struct ScheduleListItem: View {
    @State var scheduleModel: ScheduleModel
    private let stringTable = "ScheduleListView"
    let calendar = Calendar.current
    private let dateFormatter = DateFormatter()
    private let buttonLabel = "Start Observation"
    
    var body: some View {
        VStack {
            VStack(alignment: .leading) {
                ObservationDetailsButton(observationTitle: scheduleModel.observationTitle, observationType: scheduleModel.observationTitle)
                    .buttonStyle(.plain)
            }
            HStack {
                Image(systemName: "clock.fill")
                BasicText(text: .constant(String(format: "%@: %@", String.localizedString(forKey: "start", inTable: stringTable, withComment: "when the observation was started"), String(scheduleModel.start))))
                Spacer()
                BasicText(text: .constant(String(format: "Active for: %d min", 0)))
            }
            MoreActionButton {
                
            } label: {
                HStack {
                    if scheduleModel.observationType.lowercased() == "simple-question-observation" {
                        BasicText(text: .constant(String.localizedString(forKey: "start_questionnaire", inTable: stringTable, withComment: "button to start questionnaire")))
                    } else {
                        BasicText(text: .constant(String.localizedString(forKey: "start_observation", inTable: stringTable, withComment: "button to start observation")))
                    }
                }
            }.buttonStyle(PlainButtonStyle())
        }
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "simple-question-observation", observationTitle: "Test", done: false, start: 10, end: 10))
    }
}
