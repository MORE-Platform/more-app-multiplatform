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
    @StateObject var simpleQuestionViewModel: SimpleQuestionObservationViewModel = SimpleQuestionObservationViewModel()

    @State var showTaskDetails = false
    @State var showSimpleQuestion = false
    
    var scheduleModel: ScheduleModel
    var showButton: Bool

    private let stringTable = "ScheduleListView"

    var body: some View {
        VStack {
            NavigationLink(isActive: $showTaskDetails) {
                TaskDetailsView(viewModel: TaskDetailsViewModel(observationId: scheduleModel.observationId, scheduleId: scheduleModel.scheduleId, dataRecorder: viewModel.recorder), scheduleId: scheduleModel.scheduleId, scheduleListType: viewModel.scheduleListType)
            } label: {
                EmptyView()
            }.opacity(0)
            
            Button {
                showTaskDetails = true
            } label: {
                VStack(alignment: .leading) {
                    ObservationDetails(observationTitle: scheduleModel.observationTitle, observationType: scheduleModel.observationType)
                        .padding(.bottom, 4)
                    ObservationTimeDetails(start: scheduleModel.start, end: scheduleModel.end)
                }
            }

            if showButton {
                ObservationButton(showSimpleQuestion: $showSimpleQuestion, scheduleId: scheduleModel.scheduleId,
                                  observationType: scheduleModel.observationType,
                                  state: scheduleModel.scheduleState,
                                  disabled: !scheduleModel.scheduleState.active()){
                    if scheduleModel.observationType == "question-observation" {
                        showSimpleQuestion = true
                    }
                    if scheduleModel.scheduleState == ScheduleState.running {
                        viewModel.pause(scheduleId: scheduleModel.scheduleId)
                    } else {
                        viewModel.start(scheduleId: scheduleModel.scheduleId)
                    }
                }.environmentObject(simpleQuestionViewModel)
            }
        }.onAppear {
            showTaskDetails = false
        }
    }
}

extension ScheduleListItem: SimpleQuestionObservationListener {
    func onQuestionAnswered() {
        simpleQuestionViewModel.delegate = self
        DispatchQueue.main.async {
            showTaskDetails = false
            showSimpleQuestion = false
        }
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(viewModel: ScheduleViewModel(observationFactory: IOSObservationFactory(), scheduleListType: .all), scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "question-observation", observationTitle: "Test", done: false, start: 43200000, end: 43500000, scheduleState: .active), showButton: true)
    }
}
