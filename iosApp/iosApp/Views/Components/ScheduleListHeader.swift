//
//  ScheduleListHeader.swift
//  More
//
//  Created by Julia Mayrhauser on 25.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct ScheduleListHeader: View {
    @EnvironmentObject var scheduleViewModel: ScheduleViewModel
    @Binding var totalTasks: Double
    @Binding var tasksCompleted: Double
    private let stringTable = "DashboardView"
    
    var body: some View {
        VStack {
            TaskCompletionBarView(viewModel: TaskCompletionBarViewModel(), progressViewTitle: .constant(String
                .localize(forKey: "tasks_completed", withComment: "string for completed tasks", inTable: stringTable)))
            .padding(.bottom)
            MoreFilter(filterText: $scheduleViewModel.filterText) {
                    DashboardFilterView().environmentObject(scheduleViewModel.filterViewModel)
            }.onAppear { scheduleViewModel.filterText = scheduleViewModel.updateFilterText() }
            .environmentObject(scheduleViewModel.filterViewModel)
            .padding(.bottom)
        }
    }
}
