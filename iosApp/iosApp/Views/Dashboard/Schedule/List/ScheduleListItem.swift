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
    
    @State var taskDetailsIsActive = false
    @State var observationIsActive = false
    
    private let stringTable = "ScheduleListView"

    var body: some View {
        VStack {
            VStack {
                ZStack {
                    NavigationLink(isActive: $observationIsActive) {
                        QuestionObservationView().environmentObject(QuestionObservationViewModel())
                    } label: {
                        EmptyView()
                    }.opacity(0)
                }
            }
            
            NavigationLink {
                TaskDetailsView(viewModel: TaskDetailsViewModel(observationId: scheduleModel.observationId, scheduleId: scheduleModel.scheduleId, dataRecorder: viewModel.recorder))
            } label: {
                VStack(alignment: .leading) {
                    ObservationDetails(observationTitle: scheduleModel.observationTitle, observationType: scheduleModel.observationType)
                        .padding(.bottom, 4)
                    ObservationTimeDetails(start: scheduleModel.start, end: scheduleModel.end)
                }
            }

            VStack(alignment: .leading) {
                if scheduleModel.observationType == "question-observation" {
                    MoreActionButton(disabled: .constant(!scheduleModel.scheduleState.active()), action: {
                        observationIsActive = true
                    }, label: {
                        Text(String.localizedString(forKey: "start_questionnaire", inTable: stringTable, withComment: "button to start questionnaire"))
                        
                    })
                } else {
                    ObservationButton(observationType: scheduleModel.observationType,
                                      state: scheduleModel.scheduleState,
                                      disabled: !scheduleModel.scheduleState.active()){
                        if scheduleModel.scheduleState == ScheduleState.running {
                            viewModel.pause(scheduleId: scheduleModel.scheduleId)
                        } else {
                            viewModel.start(scheduleId: scheduleModel.scheduleId)
                        }
                    }
                }
            }
        }.onAppear {
            taskDetailsIsActive = false
            observationIsActive = false
        }
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(viewModel: ScheduleViewModel(observationFactory: IOSObservationFactory(), dashboardFilterViewModel: DashboardFilterViewModel()), scheduleModel: ScheduleModel(scheduleId: "schedule-id", observationId: "observation-id", observationType: "question-observation", observationTitle: "Test", done: false, start: 43200000, end: 43500000, scheduleState: .active))
    }
}
