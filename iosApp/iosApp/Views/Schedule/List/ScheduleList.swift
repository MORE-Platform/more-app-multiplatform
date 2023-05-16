//
//  ScheduleList.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared
import SwiftUI

struct ScheduleList: View {
    @ObservedObject var viewModel: ScheduleViewModel
    @EnvironmentObject var navigationModalState: NavigationModalState
    var scheduleModels: [ScheduleModel] = []
    var scheduleListType: ScheduleListType
    
    @State var limeSurveyIsPresented = false
    @State var simpleQuestionIsPresented = false
    
    var body: some View {
        ForEach(scheduleModels, id: \.scheduleId) { schedule in
            VStack {
                ScheduleListItem(viewModel: viewModel, scheduleModel: schedule, showButton: scheduleListType != .completed)
                    .environmentObject(navigationModalState)
                if schedule != scheduleModels.last {
                    Divider()
                }
            }
        }
        
    }
}

struct ScheduleList_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleList(viewModel: ScheduleViewModel(scheduleListType: .all), scheduleModels: [
            ScheduleModel(scheduleId: "id-1", observationId: "observation-id-1", observationType: "type-1", observationTitle: "title-1", done: false, start: 4000000, end: 4500000, scheduleState: .active),
            ScheduleModel(scheduleId: "id-2", observationId: "observation-id-2", observationType: "type-2", observationTitle: "title-2", done: false, start: 4000000, end: 4500000, scheduleState: .active),
        ], scheduleListType: .all)
    }
}