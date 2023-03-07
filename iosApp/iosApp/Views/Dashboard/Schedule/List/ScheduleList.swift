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
    @StateObject var model: ScheduleViewModel
    private let dateFormatter = DateFormatter()
    var body: some View {
        List {
            ForEach(Array(model.schedules.keys).sorted(), id: \.self) { key in
                VStack(alignment: .leading) {
                    BasicText(text: .constant(String(key)))
                    Divider()
                    Section {
                        ForEach(model.schedules[key]!, id: \.self) { schedule in
                                ScheduleListItem(scheduleModel: schedule)
                            }
                        }
                }.padding(.bottom)
            }
            .listRowBackground(Color.more.mainLight)
            .listRowInsets(.none)
        }
        .listStyle(.plain)
    }
}

struct ScheduleList_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleList(model: ScheduleViewModel())
    }
}

