//
//  RunningSchedules.swift
//  More
//
//  Created by Julia Mayrhauser on 19.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct RunningSchedules: View {
    var body: some View {
        ScheduleView(viewModel: ScheduleViewModel(observationFactory: IOSObservationFactory(), dashboardFilterViewModel: DashboardFilterViewModel(), runningSchedules: true))
    }
}
