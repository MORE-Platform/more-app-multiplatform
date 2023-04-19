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
    @EnvironmentObject var scheduleViewModel: ScheduleViewModel
    var body: some View {
        MoreMainBackground {
            ScheduleView(viewModel: scheduleViewModel)
        } topBarContent: {
            EmptyView()
        }.onAppear {
            scheduleViewModel.loadSchedules()
        }
    }
}
