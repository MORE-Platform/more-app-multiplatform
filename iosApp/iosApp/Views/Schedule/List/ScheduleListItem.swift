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
    @EnvironmentObject var navigationModalState: NavigationModalState
    @ObservedObject var viewModel: ScheduleViewModel
    
    var scheduleModel: ScheduleModel
    var showButton: Bool

    private let stringTable = "ScheduleListView"

    var body: some View {
        VStack {
            Button {
                navigationModalState.openView(screen: .taskDetails, scheduleId: scheduleModel.scheduleId)
            } label: {
                VStack(alignment: .leading) {
                    ObservationDetails(observationTitle: scheduleModel.observationTitle, observationType: scheduleModel.observationType)
                        .padding(.bottom, 4)
                    ObservationTimeDetails(start: scheduleModel.start, end: scheduleModel.end)
                }
            }

            if showButton && !scheduleModel.hidden {
                ObservationButton(
                    observationActionDelegate: viewModel,
                    scheduleId: scheduleModel.scheduleId,
                    observationType: scheduleModel.observationType,
                    state: scheduleModel.scheduleState,
                    disabled: !scheduleModel.scheduleState.active())
            }
        }
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(viewModel: ScheduleViewModel(scheduleListType: .all), scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "question-observation", observationTitle: "Test", done: false, start: 43200000, end: 43500000, hidden: false, scheduleState: .active), showButton: true)
    }
}
