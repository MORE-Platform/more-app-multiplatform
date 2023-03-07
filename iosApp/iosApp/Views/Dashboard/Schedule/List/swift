//
//  ScheduleListView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ScheduleListView: View {
    var model: DashboardViewModel
    var dummyList: [Int] = [1, 2, 3]
    var body: some View {
        ScheduleList(model: model)
    }
}

struct ScheduleListView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            ScheduleList(model: DashboardViewModel())
        } topBarContent: {
            EmptyView()
        }
    }
}

