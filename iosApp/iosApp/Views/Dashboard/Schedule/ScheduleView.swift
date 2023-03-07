//
//  File.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ScheduleView: View {
    @StateObject var viewModel: ScheduleViewModel
    var body: some View {
        if #available(iOS 16.0, *) {
            List {
                ForEach(Array(viewModel.schedules.keys).sorted(), id: \.self) { key in
                    VStack(alignment: .leading) {
                        BasicText(text: .constant(ScheduleViewModel.transfromInt64ToDateString(timestamp: Int64(key), dateFormat: "dd.MM.yyyy")))
                            .font(Font.more.headline)
                        Divider()
                        ScheduleList(scheduleModels: viewModel.schedules[key])
                    }.padding(.bottom)
                }
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets())
                .listRowBackground(Color.more.mainLight)
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)
        } else {
            List {
                ForEach(Array(viewModel.schedules.keys).sorted(), id: \.self) { key in
                    VStack(alignment: .leading) {
                        BasicText(text: .constant(ScheduleViewModel.transfromInt64ToDateString(timestamp: Int64(key), dateFormat: "dd.MM.yyyy")))
                            .font(Font.more.headline)
                        Divider()
                        ScheduleList(scheduleModels: viewModel.schedules[key])
                    }.padding(.bottom)
                }
                .listRowInsets(EdgeInsets())
                .listRowBackground(Color.more.mainLight)
            }
            .listStyle(.plain)
        }
    }
}

struct ScheduleView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            ScheduleView(viewModel: ScheduleViewModel())
        } topBarContent: {
            EmptyView()
        }
    }
}
