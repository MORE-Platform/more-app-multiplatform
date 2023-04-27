//
//  QuestionListItem.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import SwiftUI
import shared

struct QuestionListItem: View {
    @ObservedObject var viewModel: ScheduleViewModel
    @ObservedObject var simpleQuestionViewModel: SimpleQuestionObservationViewModel
    var schedule: ScheduleModel
    
    
    @State var taskDetailsActive = false
    @State var observationActive = false
    private let stringTable = "ScheduleListView"
    
    var body: some View {
        VStack(alignment: .leading) {
            ZStack {
                NavigationLink(isActive: $taskDetailsActive) {
                    TaskDetailsView(viewModel: TaskDetailsViewModel(observationId: schedule.observationId, scheduleId: schedule.scheduleId, dataRecorder: viewModel.recorder), simpleQuestionViewModel: simpleQuestionViewModel)
                } label: {
                    EmptyView()
                }.opacity(0)
                NavigationLink(isActive: $observationActive) {
                    SimpleQuetionObservationView(viewModel: SimpleQuestionObservationViewModel(scheduleId: schedule.scheduleId))
                } label: {
                    EmptyView()
                }.opacity(0)
            }
            Button {
                taskDetailsActive = true
            } label: {
                VStack(alignment: .leading) {
                    ObservationDetails(observationTitle: schedule.observationTitle, observationType: schedule.observationType)
                        .padding(.bottom, 4)
                    ObservationTimeDetails(start: schedule.start, end: schedule.end)
                }
            }.buttonStyle(.plain)
            MoreActionButton(disabled: .constant(!schedule.scheduleState.active()), action: {
                observationActive = true
            }, label: {
                Text(String.localizedString(forKey: "start_questionnaire", inTable: stringTable, withComment: "button to start questionnaire"))
                
                
            }).buttonStyle(.plain)
        }.onAppear {
            taskDetailsActive = false
            observationActive = false
        }
    }
}
