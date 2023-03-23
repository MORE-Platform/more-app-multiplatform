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
        VStack(alignment: .leading) {
            ObservationDetails(observationTitle: scheduleModel.observationTitle, observationType: scheduleModel.observationType)
                .padding(.bottom)
            HStack {
                Image(systemName: "calendar")
                Text(String(format: "%@ - %@", "12/03/2023", "22/03/2023"))
                    .foregroundColor(Color.more.secondary)
                // Spacer()
            }
            .padding(.bottom, 4)
            HStack {
                Image(systemName: "clock.fill")
                BasicText(text: .constant(String(format: "%@:", String.localizedString(forKey: "timeframe", inTable: stringTable, withComment: "when the observation was started"))))
                Text(String(format: "%@ - %@", scheduleModel.start.toDateString(dateFormat: "HH:mm"), scheduleModel.end.toDateString(dateFormat: "HH:mm")))
                    .foregroundColor(Color.more.secondary)
                // Spacer()
            }
            ObservationButton(observationType: scheduleModel.observationType, state: currentState) {
                if currentState == ScheduleState.running {
                    viewModel.pause(scheduleId: scheduleModel.scheduleId)
                } else {
                    viewModel.start(scheduleId: scheduleModel.scheduleId)
                }
            }
            .buttonStyle(.plain)
        }
        .padding(.bottom)
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "question-observation", observationTitle: "Test", done: false, start: 43200000, end: 43500000, currentlyRunning: false))
    }
}
