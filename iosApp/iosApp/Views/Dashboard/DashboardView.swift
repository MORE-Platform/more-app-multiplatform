//
//  DashboardView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 02.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct DashboardView: View {
    @StateObject var dashboardViewModel: DashboardViewModel
    private let stringTable = "DashboardView"
    @State var totalTasks: Double = 0
    @State var selection: Int = 0
    @State var tasksCompleted: Double = 0
    private let navigationStrings = "Navigation"
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    TaskProgressView(progressViewTitle: .constant(String
                        .localizedString(forKey: "tasks_completed", inTable: stringTable,
                                         withComment: "string for completed tasks")), totalTasks: totalTasks, tasksCompleted: tasksCompleted)
                    .padding(.bottom)
                    MoreFilter(filterText: $dashboardViewModel.filterText) {
                            DashboardFilterView().environmentObject(dashboardViewModel.scheduleViewModel.filterViewModel)
                    }
                    .padding(.bottom)
                    if selection == 0 {
                        ScheduleView(viewModel: dashboardViewModel.scheduleViewModel)
                    } else {
                        EmptyView()
                    }
                    
                }
            } topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: NavigationScreens.dashboard.localize(useTable: navigationStrings, withComment: "Dashboard title"))
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct DashboardView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            DashboardView(dashboardViewModel: DashboardViewModel(scheduleViewModel: ScheduleViewModel(observationFactory: IOSObservationFactory(), dashboardFilterViewModel: DashboardFilterViewModel(), scheduleListType: .all)))
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
            }.foregroundColor(Color.more.secondary)
        }
    }
}

