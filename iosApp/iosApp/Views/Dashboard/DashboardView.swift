//
//  DashboardView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 02.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct DashboardView: View {
    @StateObject var dashboardViewModel: DashboardViewModel
    @StateObject var scheduleViewModel: ScheduleViewModel
    private let stringTable = "DashboardView"
    @State private var totalTasks: Double = 0
    @State private var selection: Int = 0
    @State private var tasksCompleted: Double = 0
    var body: some View {
        VStack {
            StudyTitleForwardButton(title: $viewModel.studyTitle)
                .padding(.bottom)
            DashboardPicker(selection: selection, firstTab: .constant(String
                .localizedString(forKey: "schedule_string", inTable: stringTable, withComment: "schedule tab is selected")),
            secondTab: .constant(String
                .localizedString(forKey: "modules_string", inTable: stringTable, withComment: "modules tab is selected")))
                .padding(.bottom)
            MoreFilter(text: .constant(String
                .localizedString(forKey: "no_filter_activated", inTable: stringTable, withComment: "string if no filter is selected")))
            .padding(.bottom)
            TaskProgressView(progressViewTitle: .constant(String
                .localizedString(forKey: "tasks_completed", inTable: stringTable,
                                 withComment: "string for completed tasks")), totalTasks: totalTasks, tasksCompleted: tasksCompleted)
            .padding(.bottom)
            ScheduleList(model: viewModel)
        }
        .onAppear {
            viewModel.loadStudy()
        }
    }
}

struct DashboardView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            DashboardView(dashboardViewModel: DashboardViewModel(), scheduleViewModel: ScheduleViewModel())
        } topBarContent: {
            HStack {
                Button {
                } label: {
                    Image(systemName: "bell.fill")
                }
                .padding(.horizontal)
                Button {
                    
                } label: {
                    Image(systemName: "gearshape.fill")
                }
            }.foregroundColor(Color.more.icons)
        }
    }
}

