//
//  ScheduleListItem.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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
                    ObservationDetails(observationTitle: scheduleModel.observationTitle, observationType: scheduleModel.observationType, numberOfObservationErrors: viewModel.observationErrors[scheduleModel.observationType]?.count ?? 0)
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
                    disabled: !scheduleModel.scheduleState.active() || !(viewModel.observationErrors[scheduleModel.observationType]?.isEmpty ?? true))
            }
        }
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(viewModel: ScheduleViewModel(scheduleListType: .all), scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "question-observation", observationTitle: "Test", done: false, start: 43200000, end: 43500000, hidden: false, scheduleState: .active), showButton: true)
    }
}
