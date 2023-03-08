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
    @EnvironmentObject var viewModel: ScheduleViewModel 
    var body: some View {
        if #available(iOS 16.0, *) {
            List {
                ForEach(viewModel.scheduleDates, id: \.self) { key in
                    ScheduleDateWithList(key: key)
                }
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets())
                .listRowBackground(Color.more.mainLight)
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)
        } else {
            List {
                ForEach(viewModel.scheduleDates, id: \.self) { key in
                    ScheduleDateWithList(key: key)
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
            ScheduleView()
                .environmentObject(ScheduleViewModel())
        } topBarContent: {
            EmptyView()
        }
    }
}
