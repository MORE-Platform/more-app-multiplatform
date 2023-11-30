//
//  ScheduleListHeader.swift
//  More
//
//  Created by Julia Mayrhauser on 25.04.23.
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

struct ScheduleListHeader: View {
    @EnvironmentObject var scheduleViewModel: ScheduleViewModel
    @Binding var totalTasks: Double
    @Binding var tasksCompleted: Double
    private let stringTable = "DashboardView"
    
    var body: some View {
        VStack {
            TaskCompletionBarView(viewModel: TaskCompletionBarViewModel(), progressViewTitle: String.localize(forKey: "tasks_completed", withComment: "string for completed tasks", inTable:"DashboardView"))
            .padding(.bottom)
            MoreFilter(filterText: .constant("All Items")) {
                    DashboardFilterView()
                    .environmentObject(scheduleViewModel.filterViewModel)
            }.onAppear {
                
            }
            .environmentObject(scheduleViewModel.filterViewModel)
            .padding(.bottom)
        }
    }
}
