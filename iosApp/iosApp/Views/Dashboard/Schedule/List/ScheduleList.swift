//
//  ScheduleList.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared
import Foundation

struct ScheduleList: View {
    @EnvironmentObject var viewModel: ScheduleViewModel
    @State var scheduleModels: [ScheduleModel]?
    private let dateFormatter = DateFormatter()
    var body: some View {
        ForEach(scheduleModels!, id: \.scheduleId) { schedule in
            ZStack {
                ScheduleListItem(scheduleModel: schedule)
                if schedule != scheduleModels![(scheduleModels!.endIndex)-1] {
                    Divider()
                }
                NavigationLink {
                    Text("Details for scheduleid: \(schedule.observationType)")
                } label: {
                    EmptyView()
                }
                .opacity(0)
            }
        }
    }
}

struct ScheduleList_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleList(scheduleModels: [
            ScheduleModel(scheduleId: "id-1", observationId: "observation-id-1", observationType: "type-1", observationTitle: "title-1", done: false, start: 4000000, end: 4500000, currentlyRunning: false),
            ScheduleModel(scheduleId: "id-2", observationId: "observation-id-2", observationType: "type-2", observationTitle: "title-2", done: false, start: 4000000, end: 4500000, currentlyRunning: false)
        ])
    }
}

