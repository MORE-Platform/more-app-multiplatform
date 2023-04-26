//
//  CompletedSchedules.swift
//  More
//
//  Created by Julia Mayrhauser on 20.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct CompletedSchedules: View {
    @StateObject var scheduleViewModel: ScheduleViewModel
    private let navigationStrings = "Navigation"
    @State var tasksCompleted: Double = 0
    @State var totalTasks: Double = 0
    var body: some View {
        MoreMainBackground {
            VStack {
                ScheduleListHeader(totalTasks: $totalTasks, tasksCompleted: $tasksCompleted).environmentObject(scheduleViewModel)
                ScheduleView(viewModel: scheduleViewModel)
            }
            .padding(14)
        } topBarContent: {
            EmptyView()
        }
        .customNavigationTitle(with: NavigationScreens.completedObservations.localize(useTable: navigationStrings, withComment: "Completed Schedules title"))
        .navigationBarTitleDisplayMode(.inline)
    }
}
