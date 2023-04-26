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
            TaskProgressView(progressViewTitle: .constant(String
                .localizedString(forKey: "tasks_completed", inTable: stringTable,
                                 withComment: "string for completed tasks")), totalTasks: totalTasks, tasksCompleted: tasksCompleted)
            .padding(.bottom)
            MoreFilter() {
                    DashboardFilterView().environmentObject(scheduleViewModel.filterViewModel)
            }
            .environmentObject(scheduleViewModel.filterViewModel)
            .padding(.bottom)
        }
    }
}
