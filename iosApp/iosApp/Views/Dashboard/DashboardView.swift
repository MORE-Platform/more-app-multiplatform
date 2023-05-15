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
    @EnvironmentObject var contentViewModel: ContentViewModel
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
                    ScheduleListHeader(totalTasks: $totalTasks, tasksCompleted: $tasksCompleted).environmentObject(dashboardViewModel.scheduleViewModel)
                    if selection == 0 {
                        ScheduleView(viewModel: dashboardViewModel.scheduleViewModel)
                    } else {
                        EmptyView()
                    }
                }.onAppear {
                    dashboardViewModel.updateBluetoothDevices()
                }
            }
            .customNavigationTitle(with: NavigationScreens.dashboard.localize(useTable: navigationStrings, withComment: "Dashboard title"), displayMode: .inline)
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct DashboardView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            DashboardView(dashboardViewModel: DashboardViewModel(scheduleViewModel: ScheduleViewModel(scheduleListType: .all)))
                .environmentObject(ContentViewModel())
        }
    }
}

