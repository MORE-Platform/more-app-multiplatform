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
    @EnvironmentObject var viewModel: ScheduleViewModel
    @State var scheduleModels: [ScheduleModel]?
    @State var scheduleStates: [String:ScheduleState]
    private let dateFormatter = DateFormatter()
    var body: some View {
        ForEach(scheduleModels!, id: \.scheduleId) { schedule in
            VStack {
                VStack {
                    if (schedule.observationType == "question-observation") {
                        QuestionListItem(schedule: schedule).environmentObject(viewModel)
                    } else {
                        ScheduleListItem(scheduleModel: schedule)
                            .environmentObject(viewModel)
                    }
                    
                    if schedule != scheduleModels!.last {
                        Divider()
                    } else {
                        EmptyView()
                    }
                }
            }
        }
    }
}

struct ScheduleList_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleList(scheduleModels: [
            ScheduleModel(scheduleId: "id-1", observationId: "observation-id-1", observationType: "type-1", observationTitle: "title-1", done: false, start: 4000000, end: 4500000, currentlyRunning: false),
            ScheduleModel(scheduleId: "id-2", observationId: "observation-id-2", observationType: "type-2", observationTitle: "title-2", done: false, start: 4000000, end: 4500000, currentlyRunning: false)
        ], scheduleStates: [:])
    }
}

