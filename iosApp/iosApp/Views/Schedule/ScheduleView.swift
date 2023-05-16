//
//  File.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct ScheduleView: View {
    @StateObject var viewModel: ScheduleViewModel
    
    @StateObject var navigationModalState = NavigationModalState()
    
    private let stringsTable = "ScheduleListView"
    var body: some View {
        VStack {
            NavigationLink(isActive: $navigationModalState.taskDetailsOpen) {
                TaskDetailsView(viewModel: viewModel.getTaskDetailsVM(scheduleId: navigationModalState.scheduleId), scheduleId: navigationModalState.scheduleId, scheduleListType: viewModel.scheduleListType)
                    .environmentObject(navigationModalState)
            } label: {
                EmptyView()
            }.opacity(0)
            ScrollView(.vertical) {
                if (viewModel.schedulesByDate.isEmpty) {
                    if viewModel.scheduleListType == ScheduleListType.running {
                        EmptyListView(text: "No running tasks currently".localize(useTable: stringsTable, withComment: "No running tasks in list"))
                    } else if viewModel.scheduleListType == ScheduleListType.completed {
                        EmptyListView(text: "No tasks completed by now".localize(useTable: stringsTable, withComment: "No completed tasks in list"))
                    } else {
                        EmptyListView(text: "No tasks to show".localize(useTable: stringsTable, withComment: "No tasks in list shown"))
                    }
                } else {
                    LazyVStack(alignment: .leading, pinnedViews: .sectionHeaders) {
                        ForEach(viewModel.schedulesByDate.keys.sorted(), id: \.self) { key in
                            let schedules = viewModel.schedulesByDate[key, default: []]
                            if !schedules.isEmpty {
                                Section {
                                    ScheduleList(viewModel: viewModel, scheduleModels: schedules, scheduleListType: viewModel.scheduleListType)
                                        .environmentObject(navigationModalState)
                                } header: {
                                    VStack(alignment: .leading) {
                                        BasicText(text: .constant(key.formattedString()), color: Color.more.primaryDark)
                                            .font(Font.more.headline)
                                        Divider()
                                    }.background(Color.more.secondaryLight)
                                }
                                .padding(.bottom)
                            } else {
                                EmptyView()
                            }
                        }
                    }
                    .background(Color.more.secondaryLight)
                }
            }
        }
        .onAppear {
            viewModel.viewDidAppear()
            navigationModalState.taskDetailsOpen = false
        }
        .onDisappear {
            viewModel.viewDidDisappear()
        }
        .fullScreenCover(isPresented: $navigationModalState.simpleQuestionOpen) {
            SimpleQuetionObservationView(viewModel: viewModel.getSimpleQuestionObservationVM(scheduleId: navigationModalState.scheduleId))
                .environmentObject(navigationModalState)
        }
        .fullScreenCover(isPresented: $navigationModalState.simpleQuestionThankYouOpen) {
            SimpleQuestionThankYouView()
                .environmentObject(navigationModalState)
        }
        .fullScreenCover(isPresented: $navigationModalState.limeSurveyOpen) {
            LimeSurveyView(viewModel: LimeSurveyViewModel(scheduleId: navigationModalState.scheduleId))
                .environmentObject(navigationModalState)
        }
    }
}

struct ScheduleView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            ScheduleView(viewModel: ScheduleViewModel(scheduleListType: .all))
        } 
    }
}
