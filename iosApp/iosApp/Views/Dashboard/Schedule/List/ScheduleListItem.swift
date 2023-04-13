//
//  ScheduleListItem.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation

import shared
import SwiftUI

struct ScheduleListItem: View {
    @ObservedObject var viewModel: ScheduleViewModel
    var scheduleModel: ScheduleModel
    
    private let stringTable = "ScheduleListView"

    var body: some View {
        VStack {
            VStack {
                ZStack {
                    VStack(alignment: .leading) {
                        ObservationDetails(observationTitle: scheduleModel.observationTitle, observationType: scheduleModel.observationType)
                            .padding(.bottom, 4)
                        ObservationTimeDetails(start: scheduleModel.start, end: scheduleModel.end)
                    }
                    NavigationLink {
                        TaskDetailsView(viewModel: TaskDetailsViewModel(observationId: scheduleModel.observationId, scheduleId: scheduleModel.scheduleId, dataRecorder: viewModel.recorder))
                    } label: {
                        EmptyView()
                    }
                    .opacity(0)
                }
            }

            VStack(alignment: .leading) {
                ObservationButton(observationType: scheduleModel.observationType,
                                  state: scheduleModel.scheduleState,
                                  disabled: !scheduleModel.scheduleState.active()){
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
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(viewModel: ScheduleViewModel(observationFactory: IOSObservationFactory()), scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "question-observation", observationTitle: "Test", done: false, start: 43200000, end: 43500000, scheduleState: .active))
    }
}
