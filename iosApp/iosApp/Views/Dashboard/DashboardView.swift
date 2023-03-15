//
//  DashboardView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 02.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct DashboardView: View {
    @StateObject var dashboardViewModel: DashboardViewModel
    private let stringTable = "DashboardView"
    @State var totalTasks: Double = 0
    @State var selection: Int = 0
    @State var tasksCompleted: Double = 0
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    Title(titleText: $dashboardViewModel.studyTitle)
                        .padding(.bottom)
                        
                    MoreFilter(text: .constant(String
                        .localizedString(forKey: "no_filter_activated", inTable: stringTable, withComment: "string if no filter is selected")))
                    .padding(.bottom)
                    TaskProgressView(progressViewTitle: .constant(String
                        .localizedString(forKey: "tasks_completed", inTable: stringTable,
                                         withComment: "string for completed tasks")), totalTasks: totalTasks, tasksCompleted: tasksCompleted)
                    .padding(.bottom)
                    if selection == 0 {
                        ScheduleView()
                            .environmentObject(dashboardViewModel.scheduleViewModel)
                    } else {
                        EmptyView()
                    }
                }
            } topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: "Dashboard")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct DashboardView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            DashboardView(dashboardViewModel: DashboardViewModel())
        } topBarContent: {
            HStack {
                Button {
                } label: {
                    Image(systemName: "bell.fill")
                }
                .padding(.horizontal)
                Button {
                    
                } label: {
                    Image(systemName: "gearshape.fill")
                }
            }.foregroundColor(Color.more.icons)
        }
    }
}

