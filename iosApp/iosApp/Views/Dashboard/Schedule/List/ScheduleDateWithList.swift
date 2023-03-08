//
//  ScheduleDateList.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ScheduleDateWithList: View {
    @EnvironmentObject var viewModel: ScheduleViewModel
    var key: UInt64
    var body: some View {
        VStack(alignment: .leading) {
            BasicText(text: .constant(Int64(key).toDateString(dateFormat: "dd.MM.yyyy")))
                .font(Font.more.headline)
            Divider()
            ScheduleList(scheduleModels: viewModel.schedules[key])
        }.padding(.bottom)
    }
}
