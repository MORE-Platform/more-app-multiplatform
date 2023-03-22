//
//  TaskDetailView.swift
//  iosApp
//
//  Created by Isabella Aigner on 17.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct TaskDetailView: View {
    @StateObject var taskDetailViewModel: TaskDetailViewModel
    //@State var taskComleted: Double = 0
    private let stringTable = "TaskDetail"
    private let navigationStrings = "Navigation"
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(
                    spacing: 30
                ) {
                    VStack{
                        HStack{
                            Title2(titleText: $taskDetailViewModel.observationTitle)
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
                            BasicText(text: $taskDetailViewModel.observationType, color: .more.secondary)
                            Spacer()
                        }
                    }
                    
                    VStack {
                        HStack {
                            Image(systemName: "calendar")
                                .padding(0.5)
                            BasicText(text: $taskDetailViewModel.observationDates, color: .more.secondary)
                                .padding(1)
                            Spacer()
                            Image(systemName: "repeat")
                                .padding(0.7)
                            BasicText(text: $taskDetailViewModel.observationRepetitionInterval, color: .more.secondary)
                                .padding(1)
                        }
                        HStack {
                            Image(systemName: "clock.fill")
                                .padding(0.7)
                            Text(String.localizedString(forKey: "Timeframe", inTable: stringTable, withComment: "Timeframe of observation"))
                                .foregroundColor(.more.primary)
                            BasicText(text: $taskDetailViewModel.observationTimeframe, color: .more.secondary)
                            Spacer()
                        }
                    }
                    
                    
                    HStack {
                        AccordionItem(title: String.localizedString(forKey: "Participant Information", inTable: stringTable, withComment: "Participant Information of specific task."), info: $taskDetailViewModel.participantInfo)
                    }
                    Spacer()
                    HStack{
                        DatapointsCollection(datapoints: $taskDetailViewModel.observationDatapoints)
                    }
                    Spacer()
                    MoreActionButton(action: {}, label: {
                        Text("Pause Observation")
                    })
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

struct TaskDetailsView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            DashboardView(dashboardViewModel: DashboardViewModel())
        } topBarContent: {
            HStack {
                // center text for view title
            }.foregroundColor(Color.more.secondary)
        }
    }
}