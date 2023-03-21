//
//  TaskDetailsView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct TaskDetailsView: View {
    
    @StateObject var viewModel: TaskDetailsViewModel
    @EnvironmentObject var scheduleViewModel: ScheduleViewModel
    @State var count: Int64 = 0
    
    var body: some View {
        VStack {
            Text(viewModel.taskDetailsModel?.observationTitle ?? "")
            ObservationButton(observationType: viewModel.taskDetailsModel?.observationType ?? "", state: scheduleViewModel.scheduleStates[viewModel.taskDetailsModel?.scheduleId ?? ""] ?? ScheduleState.non) {
                let scheduleId = viewModel.taskDetailsModel?.scheduleId ?? ""
                if scheduleViewModel.scheduleStates[scheduleId] == ScheduleState.running {
                    scheduleViewModel.pause(scheduleId: scheduleId)
                } else {
                    scheduleViewModel.start(scheduleId: scheduleId)
                    count = viewModel.dataPointCount?.count ?? 0
                }
            }
            Text("Count: \(count)")
        }
    }
}

