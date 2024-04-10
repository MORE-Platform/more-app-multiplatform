//
//  DashboardView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 02.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import SwiftUI
import shared

struct DashboardView: View {
    @EnvironmentObject var contentViewModel: ContentViewModel
    @StateObject var viewModel: DashboardViewModel
    private let stringTable = "DashboardView"
    @State var totalTasks: Double = 0
    @State var selection: Int = 0
    @State var tasksCompleted: Double = 0
    private let navigationStrings = "Navigation"
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    ScheduleListHeader(totalTasks: $totalTasks, tasksCompleted: $tasksCompleted)
                        .environmentObject(viewModel.scheduleViewModel)
                    if selection == 0 {
                        ScheduleView(viewModel: viewModel.scheduleViewModel)
                    } else {
                        EmptyView()
                    }
                }
            }
            .customNavigationTitle(with: NavigationScreens.dashboard.localize(useTable: navigationStrings, withComment: "Dashboard title"), displayMode: .inline)
        }
        .onAppear {
            viewModel.viewDidAppear()
        }
        .onDisappear {
            viewModel.viewDidDisappear()
        }
    }
}

struct DashboardView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            DashboardView(viewModel: DashboardViewModel(scheduleViewModel: ScheduleViewModel(scheduleListType: .all)))
                .environmentObject(ContentViewModel())
        }
    }
}

