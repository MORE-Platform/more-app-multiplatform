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
    private let buttonLabel = "Start Observation"
    
    var body: some View {
        VStack {
            ObservationDetailsButton(observationTitle: scheduleModel.observationTitle, observationType: scheduleModel.observationTitle)
                .padding(0.5)
            HStack {
                Image(systemName: "clock.fill")
                BasicText(text: .constant(String(format: "%@:", String.localizedString(forKey: "start", inTable: stringTable, withComment: "when the observation was started"))))
                BasicText(text: .constant(ScheduleViewModel.transfromInt64ToDateString(timestamp: scheduleModel.start, dateFormat: "HH:mm")))
                    .foregroundColor(Color.more.icons)
                Spacer()
                Image(systemName: "clock.arrow.circlepath")
                BasicText(text: .constant(String(format: "%@:", String.localizedString(forKey: "active_for", inTable: stringTable, withComment: "how long the observation has been active for"), 0)))
                BasicText(text: .constant(String(format: "%d min", (scheduleModel.end - scheduleModel.start) / 60000)))
                    .foregroundColor(Color.more.icons)
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
        }.padding(.bottom)
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "simple-question-observation", observationTitle: "Test", done: false, start: 43200000, end: 43500000))
    }
}
