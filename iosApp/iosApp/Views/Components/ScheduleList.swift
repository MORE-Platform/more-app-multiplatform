//
//  ScheduleList.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ScheduleList: View {
    var model: DashboardViewModel
    var dummyList: [Int] = [1, 2, 3]
    var body: some View {
        ScrollView {
            ForEach(dummyList, id: \.self) { item in
                VStack {
                    ScheduleListItem(observationTitle: "Test", observationType: "simple-question-observation", scheduleStart: Date(), scheduleEnd: Date(), observation: ObservationSchema(), activeFor: 0)
                        .listRowBackground(Color.more.mainLight)
                    Divider()
                }
            }
        }
    }
}

struct ScheduleList_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            ScheduleList(model: DashboardViewModel())
        } topBarContent: {
            EmptyView()
        }
    }
}


