//
//  CompletedSchedules.swift
//  More
//
//  Created by Julia Mayrhauser on 20.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct CompletedSchedules: View {
    @EnvironmentObject var scheduleViewModel: ScheduleViewModel
    var body: some View {
        MoreMainBackground {
            ScheduleView(viewModel: scheduleViewModel, scheduleListType: .completed)
        } topBarContent: {
            EmptyView()
        }
    }
}
