//
//  TaskDetailsView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 20.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared
import SwiftUI

struct TaskDetailsView: View {
    @StateObject var viewModel: TaskDetailsViewModel
    @State var count: Int64 = 0
    var scheduleId: String = ""
    var scheduleListType: ScheduleListType
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
                            Title2(titleText: .constant(viewModel.taskDetailsModel?.observationTitle ?? ""))
                                .padding(0.5)
                            // abort button
                            Spacer()
                            if viewModel.taskDetailsModel?.state == ScheduleState.running {
                                InlineAbortButton {
                                    viewModel.stop()
                                }
                            }
                        }
                        .frame(height: 40)
                        HStack(
                        ) {
                            BasicText(text: .constant(viewModel.taskDetailsModel?.observationType ?? ""), color: .more.secondary)
                            Spacer()
                        }
                    }

                    let date: String = viewModel.getDateRangeString()
                    let time: String = viewModel.getTimeRangeString()

                    ObservationDetailsData(dateRange: .constant(date), repetition: $viewModel.observationRepetitionInterval, timeframe: .constant(time))

                    HStack {
                        AccordionItem(title: String.localizedString(forKey: "Participant Information", inTable: stringTable, withComment: "Participant Information of specific task."), info: .constant(viewModel.taskDetailsModel?.participantInformation ?? ""))
                    }
                    if viewModel.taskDetailsModel?.observationType != "question-observation" {
                        Spacer()
                        HStack {
                            if let task = viewModel.taskDetailsModel {
                                DatapointsCollection(datapoints: $viewModel.dataCount, running: task.state == .running)
                            }
                        }
                        Spacer()
                    }
                    if scheduleListType != .completed {
                            if let model = viewModel.taskDetailsModel {
                                if model.observationType == "question-observation" {
                                    NavigationLinkButton(disabled: .constant(model.state != .active && model.state != .running && model.state != .paused && (Date(timeIntervalSince1970: TimeInterval(model.start)) > Date() || Date(timeIntervalSince1970: TimeInterval(model.end)) <= Date()))
                                    ) {
                                        VStack {
                                            if scheduleId != "" {
                                                SimpleQuetionObservationView(viewModel: SimpleQuestionObservationViewModel(scheduleId: scheduleId))
                                            } else {
                                                EmptyView()
                                            }
                                        }

                                    } label: {
                                        Text(String.localizedString(forKey: "start_questionnaire", inTable: scheduleStringTable, withComment: "Button to start a questionnaire"))
                                            .foregroundColor(Date(timeIntervalSince1970: TimeInterval(viewModel.taskDetailsModel?.start ?? 0)) < Date() && Date() < Date(timeIntervalSince1970: TimeInterval(viewModel.taskDetailsModel?.start ?? 0)) ? .more.secondaryMedium : .more.white)
                                    }
                                }
                                else {
                                    ObservationButton(scheduleId: scheduleId,
                                                    observationType: model.observationType, state: model.state, disabled: model.state != .active
                                                      && model.state != .running
                                                      && model.state != .paused
                                                      && (Date(timeIntervalSince1970: TimeInterval(model.start)) > Date()
                                                          || Date(timeIntervalSince1970: TimeInterval(model.end)) <= Date())) {
                                        if model.state == .running {
                                            viewModel.pause()
                                        } else {
                                            viewModel.start()
                                        }
                                    }
                                }
                        }
                    }
                    Spacer()
                }

            } topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: NavigationScreens.taskDetails.localize(useTable: navigationStrings, withComment: "Task Detail"))
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
