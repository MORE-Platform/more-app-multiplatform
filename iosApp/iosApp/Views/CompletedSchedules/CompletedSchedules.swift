//
//  CompletedSchedules.swift
//  More
//
//  Created by Julia Mayrhauser on 20.04.23.
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

struct CompletedSchedules: View {
    @StateObject var scheduleViewModel: ScheduleViewModel
    private let navigationStrings = "Navigation"
    @State var tasksCompleted: Double = 0
    @State var totalTasks: Double = 0
    var body: some View {
        MoreMainBackgroundView {
            VStack {
                ScheduleListHeader(totalTasks: $totalTasks, tasksCompleted: $tasksCompleted).environmentObject(scheduleViewModel)
                ScheduleView(viewModel: scheduleViewModel)
            }
            .padding(14)
        }
        .customNavigationTitle(with: NavigationScreens.pastObservations.localize(useTable: navigationStrings, withComment: "Completed Schedules title"),displayMode: .inline)
    }
}
