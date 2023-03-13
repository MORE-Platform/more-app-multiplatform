//
//  ScheduleListItem.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import RealmSwift
import shared
import SwiftUI

struct ScheduleListItem: View {
    @EnvironmentObject var viewModel: ScheduleViewModel
    @State var scheduleModel: ScheduleModel
    private let stringTable = "ScheduleListView"

    var body: some View {
        let currentState = viewModel.scheduleStates[scheduleModel.scheduleId] ?? ScheduleState.non
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
            ObservationButton(observationType: scheduleModel.observationType, state: currentState) {
                if currentState == ScheduleState.running {
                    viewModel.pause(scheduleId: scheduleModel.scheduleId)
                } else {
                    viewModel.start(scheduleModel: self.scheduleModel)
                }
            }
            .buttonStyle(.plain)
        }.padding(.bottom)
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "question-observation", observationTitle: "Test", done: false, start: 43200000, end: 43500000, config: [:], currentlyRunning: false))
    }
}
