//
//  TaskDetailsView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct TaskDetailsView: View {
    @StateObject var viewModel: TaskDetailsViewModel
    @State var count: Int64 = 0
    private let stringTable = "TaskDetail"
    private let navigationStrings = "Navigation"
        
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(
                    spacing: 20
                ) {
                    VStack{
                        HStack{
                            Title2(titleText: .constant(viewModel.taskDetailsModel?.observationTitle ?? ""))
                                .padding(0.5)
                            // abort button
                            Spacer()
                            if viewModel.taskDetailsModel?.state == ScheduleState.running {
                                InlineAbortButton() {
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
                    
                    
                    let date: String = (viewModel.taskDetailsModel?.start.toDateString(dateFormat: "dd.MM.yyyy") ?? "") + " - " + (viewModel.taskDetailsModel?.end.toDateString(dateFormat: "dd.MM.yyyy") ?? "")
                    let time: String = (viewModel.taskDetailsModel?.start.toDateString(dateFormat: "HH:mm") ?? "") + " - " + (viewModel.taskDetailsModel?.end.toDateString(dateFormat: "HH:mm") ?? "")
                    
                    ObservationDetailsData(dateRange: .constant(date), repetition: $viewModel.observationRepetitionInterval, timeframe: .constant(time))
                    
                    HStack{
                        AccordionItem(title: String.localizedString(forKey: "Participant Information", inTable: stringTable, withComment: "Participant Information of specific task."), info: .constant(viewModel.taskDetailsModel?.participantInformation ?? ""))
                    }
                    if viewModel.taskDetailsModel?.observationType != "question-observation" {
                        Spacer()
                        HStack{
                            DatapointsCollection(datapoints: .constant(viewModel.taskDetailsModel?.dataPointCount ?? 0), running: .constant(viewModel.taskDetailsModel?.state == ScheduleState.running))
                            
                        }
                        Spacer()
                    }
                   
                    if let model = viewModel.taskDetailsModel {
                        ObservationButton(observationType: model.observationType,
                                          state: .constant(),
                                          disabled: model.state != .active
                                          && model.state != .running
                                          && model.state != .paused
                                          && (Date(timeIntervalSince1970: TimeInterval(model.start)) > Date()
                                          || Date(timeIntervalSince1970: TimeInterval(model.end)) <= Date())) {
                            if viewModel.taskDetailsModel?.state == ScheduleState.running {
                                viewModel.pause()
                            } else {
                                viewModel.start()
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

