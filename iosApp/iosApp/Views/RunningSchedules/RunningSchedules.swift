//
//  RunningSchedules.swift
//  More
//
//  Created by Julia Mayrhauser on 19.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct RunningSchedules: View {
    @StateObject var scheduleViewModel: ScheduleViewModel
    @State var totalTasks: Double = 0
    @State var tasksCompleted: Double = 0
    private let navigationStrings = "Navigation"
    var body: some View {
        MoreMainBackgroundView {
            VStack {
                ScheduleListHeader(totalTasks: $totalTasks, tasksCompleted: $tasksCompleted).environmentObject(scheduleViewModel)
                ScheduleView(viewModel: scheduleViewModel)
            }.padding(14)
        } 
        .customNavigationTitle(with: NavigationScreens.runningObservations.localize(useTable: navigationStrings, withComment: "Running Schedules title"), displayMode: .inline)
    }    
}
