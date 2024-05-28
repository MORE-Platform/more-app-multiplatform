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
    @ObservedObject var scheduleViewModel: ScheduleViewModel
    @Binding var totalTasks: Double
    @Binding var tasksCompleted: Double
    
    @EnvironmentObject var navigationModalState: NavigationModalState
    private let stringTable = "DashboardView"

    var body: some View {
        VStack {
            TaskCompletionBarView(viewModel: TaskCompletionBarViewModel(), progressViewTitle: String.localize(forKey: "tasks_completed", withComment: "string for completed tasks", inTable: "DashboardView"))
                .padding(.bottom)
            if scheduleViewModel.numberOfObservationErrors() > 0 {
                MoreActionButton(backgroundColor: .more.important, disabled: .constant(false)) {
                    navigationModalState.openView(screen: .observationErrors)
                } label: {
                    HStack {
                        Image(systemName: "exclamationmark.triangle")
                            .padding(.trailing, 2)
                        Text("\(scheduleViewModel.numberOfObservationErrors()) \("errors".localize(withComment: "Errors", useTable: "Errors"))")
                    }
                }
                .padding(.bottom)
            }
            MoreFilter(filterText: .constant("All Items"), destination: .dashboardFilter)
                .padding(.bottom)
        }
    }
}
