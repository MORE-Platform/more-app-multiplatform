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
    
    var body: some View {
        VStack {
            Button {} label: {
                ObservationDetails(observationTitle: scheduleModel.observationTitle, observationType: scheduleModel.observationType)
            }.buttonStyle(.plain)
            .padding(0.5)
            
            HStack {
                Image(systemName: "clock.fill")
                BasicText(text: .constant(String(format: "%@:", String.localizedString(forKey: "start", inTable: stringTable, withComment: "when the observation was started"))))
                Text(scheduleModel.start.toDateString(dateFormat: "HH:mm"))
                    .foregroundColor(Color.more.icons)
                Spacer()
                Image(systemName: "clock.arrow.circlepath")
                BasicText(text: .constant(String(format: "%@:", String.localizedString(forKey: "active_for", inTable: stringTable, withComment: "how long the observation has been active for"), 0)))
                Text(String(format: "%d min", (scheduleModel.end - scheduleModel.start) / 60000))
                    .foregroundColor(Color.more.icons)
            }
            StartObservationButton(observationType: scheduleModel.observationType)
                .buttonStyle(.plain)
        }.padding(.bottom)
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "question-observation", observationTitle: "Test", done: false, start: 43200000, end: 43500000))
    }
}