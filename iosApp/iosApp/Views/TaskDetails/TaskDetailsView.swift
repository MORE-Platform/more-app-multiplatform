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
    @EnvironmentObject var scheduleViewModel: ScheduleViewModel
    @State var count: Int64 = 0
    private let stringTable = "TaskDetail"
    private let navigationStrings = "Navigation"
        
    var body: some View {
        let scheduleId = viewModel.taskDetailsModel?.scheduleId ?? ""
        
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
                            if #available(iOS 15.0, *) {
                                InlineAbortButton()
                            } else {
                                InlineAbortButtonIOS14()
                            }
                        }
                        HStack(
                        ) {
                            BasicText(text: .constant(viewModel.taskDetailsModel?.observationType ?? ""), color: .more.secondary)
                            Spacer()
                        }
                    }
                    
                    VStack {
                        HStack {
                            if(viewModel.taskDetailsModel?.start != nil && viewModel.taskDetailsModel?.end != nil) {
                                Image(systemName: "calendar")
                                    .padding(0.5)
                                
                                let date: String = (viewModel.taskDetailsModel?.start.toDateString(dateFormat: "dd/MM/yyyy") ?? "") + " - " + (viewModel.taskDetailsModel?.end.toDateString(dateFormat: "dd/MM/yyyy") ?? "")
                                BasicText(text: .constant(date), color: .more.secondary)
                                    .padding(1)
                                
                                Spacer()
                            }
                            
                            Image(systemName: "repeat")
                                .padding(0.7)
                            BasicText(text: $viewModel.observationRepetitionInterval, color: .more.secondary)
                                .padding(1)
                        }
                        HStack {
                            if(viewModel.taskDetailsModel?.start != nil && viewModel.taskDetailsModel?.end != nil) {
                                Image(systemName: "clock.fill")
                                    .padding(0.7)
                                Text(String.localizedString(forKey: "Timeframe", inTable: stringTable, withComment: "Timeframe of observation"))
                                    .foregroundColor(.more.primary)
                                let time: String = (viewModel.taskDetailsModel?.start.toDateString(dateFormat: "HH:mm") ?? "") + " - " + (viewModel.taskDetailsModel?.end.toDateString(dateFormat: "HH:mm") ?? "")
                                
                                BasicText(text: .constant(time), color: .more.secondary)
                                Spacer()
                            }
                        }
                    }
                    
                    
                    HStack{
                        AccordionItem(title: String.localizedString(forKey: "Participant Information", inTable: stringTable, withComment: "Participant Information of specific task."), info: .constant(viewModel.taskDetailsModel?.participantInformation ?? ""))
                    }
                    if viewModel.taskDetailsModel?.observationType != "question-observation" {
                        Spacer()
                        HStack{
                            DatapointsCollection(datapoints: $count, running: .constant(scheduleViewModel.scheduleStates[scheduleId] == ScheduleState.running))
                            
                        }
                        Spacer()
                    }
                   
                    ObservationButton(observationType: viewModel.taskDetailsModel?.observationType ?? "", state: scheduleViewModel.scheduleStates[viewModel.taskDetailsModel?.scheduleId ?? ""] ?? ScheduleState.non) {
                            let scheduleId = viewModel.taskDetailsModel?.scheduleId ?? ""
                            if scheduleViewModel.scheduleStates[scheduleId] == ScheduleState.running {
                                scheduleViewModel.pause(scheduleId: scheduleId)
                            } else {
                                scheduleViewModel.start(scheduleId: scheduleId)
                                count = viewModel.dataPointCount?.count ?? 0
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

