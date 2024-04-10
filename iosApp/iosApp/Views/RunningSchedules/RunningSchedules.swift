//
//  RunningSchedules.swift
//  More
//
//  Created by Julia Mayrhauser on 19.04.23.
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

struct RunningSchedules: View {
    @StateObject var scheduleViewModel: ScheduleViewModel
    @State var totalTasks: Double = 0
    @State var tasksCompleted: Double = 0
    private let navigationStrings = "Navigation"
    var body: some View {
        VStack {
            ScheduleListHeader(totalTasks: $totalTasks, tasksCompleted: $tasksCompleted)
            ScheduleView(viewModel: scheduleViewModel)
        }
        .customNavigationTitle(with: NavigationScreen.runningObservations.localize(useTable: navigationStrings, withComment: "Running Schedules title"), displayMode: .inline)
    }
}
