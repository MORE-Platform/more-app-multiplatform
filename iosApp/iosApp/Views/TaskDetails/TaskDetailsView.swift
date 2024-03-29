//
//  TaskDetailsView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 20.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import shared
import SwiftUI

struct TaskDetailsView: View {
    @StateObject var viewModel: TaskDetailsViewModel
    var scheduleListType: ScheduleListType
    
    @EnvironmentObject var navigationModalState: NavigationModalState
    
    
    private let stringTable = "TaskDetail"
    private let scheduleStringTable = "ScheduleListView"
    private let navigationStrings = "Navigation"

    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(
                    spacing: 20
                ) {
                    VStack {
                        HStack {
                            Title2(titleText: viewModel.taskDetailsModel?.observationTitle ?? "")
                                .padding(0.5)
                            Spacer()
                            if viewModel.taskDetailsModel?.state == ScheduleState.running, let scheduleId = navigationModalState.navigationState.scheduleId {
                                InlineAbortButton {
                                    viewModel.stop(scheduleId: scheduleId)
                                }
                            }
                        }
                        .frame(height: 40)
                        HStack(
                        ) {
                            BasicText(text: viewModel.taskDetailsModel?.observationType ?? "", color: .more.secondary)
                            Spacer()
                        }
                    }

                    let date: String = viewModel.getDateRangeString()
                    let time: String = viewModel.getTimeRangeString()

                    ObservationDetailsData(dateRange: date, timeframe: time)

                    HStack {
                        AccordionItem(title: String.localize(forKey: "Participant Information", withComment: "Participant Information of specific task.", inTable: stringTable), info: viewModel.taskDetailsModel?.participantInformation ?? "")
                    }
                    if scheduleListType != .completed {
                        Spacer()
                        HStack {
                            if let task = viewModel.taskDetailsModel {
                                DatapointsCollection(datapoints: $viewModel.dataCount, running: task.state == .running)
                            }
                        }
                        Spacer()
                    }
                    if scheduleListType != .completed && !(viewModel.taskDetailsModel?.hidden ?? true) {
                        if let model = viewModel.taskDetailsModel, let scheduleId = navigationModalState.navigationState.scheduleId {
                            ObservationButton(
                                observationActionDelegate: viewModel,
                                scheduleId: scheduleId,
                                observationType: model.observationType,
                                state: model.state,
                                disabled: !model.state.active())
                        }
                    }
                    Spacer()
                }

            }
            .customNavigationTitle(with: NavigationScreens.taskDetails.localize(useTable: navigationStrings, withComment: "Task Detail"))
            .onAppear {
                viewModel.viewDidAppear()
            }
            .onDisappear {
                viewModel.viewDidDisappear()
            }
        }
    }
}

extension TaskDetailsView: SimpleQuestionObservationListener {
    func onQuestionAnswered() {
        // self.presentationMode.wrappedValue.dismiss()
    }
}
