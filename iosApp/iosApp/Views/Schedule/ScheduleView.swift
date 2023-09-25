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
                        EmptyListView(text: "No running tasks currently".localize(withComment: "No running tasks in list", useTable: stringsTable))
                    } else if viewModel.scheduleListType == ScheduleListType.completed {
                        EmptyListView(text: "No tasks completed by now".localize(withComment: "No completed tasks in list", useTable: stringsTable))
                    } else {
                        EmptyListView(text: "No tasks to show".localize(withComment: "No tasks in list shown", useTable: stringsTable))
                    }
                } else {
                    LazyVStack(alignment: .leading, pinnedViews: .sectionHeaders) {
                        ForEach(viewModel.schedulesByDate.keys.sorted(), id: \.self) { key in
                            let schedules = viewModel.schedulesByDate[key, default: []]
                            if !schedules.isEmpty {
                                Section {
                                    ForEach(schedules, id: \.scheduleId) { schedule in
                                        VStack {
                                            ScheduleListItem(viewModel: viewModel, scheduleModel: schedule, showButton: viewModel.scheduleListType != .completed)
                                                .environmentObject(navigationModalState)
                                            Divider()
                                        }
                                    }
                                } header: {
                                    VStack(alignment: .leading) {
                                        BasicText(text: key.formattedString(), color: Color.more.primaryDark)
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
        .fullScreenCover(isPresented: $navigationModalState.selfLearningQuestionOpen) {
            SelfLearningMultipleChoiceQuestionView(viewModel: viewModel.getSelfLearningMultipleChoiceQuestionObservationVM(scheduleId: navigationModalState.scheduleId))
                .environmentObject(navigationModalState)
        }
        .fullScreenCover(isPresented: $navigationModalState.selfLearningQuestionThankYouOpen) {
            SelfLearningMultipleChoiceQuestionThankYouView()
                .environmentObject(navigationModalState)
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
            LimeSurveyView(viewModel: LimeSurveyViewModel(navigationModalState: navigationModalState))
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
