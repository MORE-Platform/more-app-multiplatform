//
//  ScheduleList.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import shared
import SwiftUI

struct ScheduleList: View {
    @ObservedObject var viewModel: ScheduleViewModel
    @EnvironmentObject var navigationModalState: NavigationModalState
    var scheduleModels: [ScheduleModel] = []
    var scheduleListType: ScheduleListType
    
    var body: some View {
        ForEach(scheduleModels, id: \.scheduleId) { schedule in
            VStack {
                ScheduleListItem(viewModel: viewModel, scheduleModel: schedule, showButton: scheduleListType != .completed)
                    .environmentObject(navigationModalState)
                Divider()
            }
        }
        
    }
}

struct ScheduleList_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleList(viewModel: ScheduleViewModel(scheduleListType: .all), scheduleModels: [
            ScheduleModel(scheduleId: "id-1", observationId: "observation-id-1", observationType: "type-1", observationTitle: "title-1", done: false, start: 4000000, end: 4500000, hidden: false, scheduleState: .active),
            ScheduleModel(scheduleId: "id-2", observationId: "observation-id-2", observationType: "type-2", observationTitle: "title-2", done: false, start: 4000000, end: 4500000, hidden: false, scheduleState: .active),
        ], scheduleListType: .all)
    }
}
