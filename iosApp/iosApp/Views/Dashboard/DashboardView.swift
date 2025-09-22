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

import shared
import SwiftUI

struct DashboardView: View {
    @EnvironmentObject private var navigationModalState: NavigationModalState
    @StateObject var viewModel: ScheduleViewModel
    private let stringTable = "DashboardView"
    @State var totalTasks: Double = 0
    @State var selection: Int = 0
    @State var tasksCompleted: Double = 0
    private let navigationStrings = "Navigation"
    var body: some View {
        VStack {
            ScheduleListHeader(scheduleViewModel: viewModel, totalTasks: $totalTasks, tasksCompleted: $tasksCompleted)
            if selection == 0 {
                ScheduleView(viewModel: viewModel)
            } else {
                EmptyView()
            }
        }
        .customNavigationTitle(with: NavigationScreen.dashboard.localize(), displayMode: .inline)
    }
}

struct DashboardView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            DashboardView(viewModel: ScheduleViewModel(scheduleListType: .all))
                .environmentObject(ContentViewModel())
        }
    }
}
