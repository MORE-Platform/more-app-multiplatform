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
    @ObservedObject var viewModel: ScheduleViewModel
    @State var scheduleModel: ScheduleModel
    private let stringTable = "ScheduleListView"

    var body: some View {
        VStack(alignment: .leading) {
            ObservationDetails(observationTitle: scheduleModel.observationTitle, observationType: scheduleModel.observationType)
                .padding(.bottom, 4)
            HStack {
                Image(systemName: "clock.fill")
                BasicText(text: .constant(String(format: "%@:", String.localizedString(forKey: "timeframe", inTable: stringTable, withComment: "when the observation was started"))))
                Text(String(format: "%@ - %@", scheduleModel.start.toDateString(dateFormat: "HH:mm"), scheduleModel.end.toDateString(dateFormat: "HH:mm")))
                    .foregroundColor(Color.more.secondary)
            }
            ObservationButton(observationType: scheduleModel.observationType, state: $scheduleModel.scheduleState, disabled: scheduleModel.scheduleState != .active
                              && scheduleModel.scheduleState != .running
                              && scheduleModel.scheduleState != .paused
                              && (Date(timeIntervalSince1970: TimeInterval(scheduleModel.start)) > Date()
                              || Date(timeIntervalSince1970: TimeInterval(scheduleModel.end)) <= Date())) {
                if scheduleModel.scheduleState == ScheduleState.running {
                    viewModel.pause(scheduleId: scheduleModel.scheduleId)
                } else {
                    viewModel.start(scheduleId: scheduleModel.scheduleId)
                }
            }
            .buttonStyle(.plain)
        }
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(viewModel: ScheduleViewModel(observationFactory: IOSObservationFactory()), scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "question-observation", observationTitle: "Test", done: false, start: 43200000, end: 43500000, scheduleState: .active))
    }
}
