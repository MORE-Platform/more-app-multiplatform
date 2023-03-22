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
            List(viewModel.scheduleDates, id: \.self) { key in
                Section {
                    ScheduleList(scheduleModels: viewModel.schedules[key], scheduleStates: viewModel.scheduleStates)
                        .environmentObject(viewModel)
                } header: {
                    BasicText(text: .constant(Int64(key).toDateString(dateFormat: "dd.MM.yyyy")))
                        .font(Font.more.headline)
                }
                .padding(.bottom)
                .hideListRowSeparator()
                .listRowInsets(EdgeInsets())
                .listRowBackground(Color.more.primaryLight)

            }
            .listStyle(.plain)
            .clearListBackground()

    }
}

struct ScheduleView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            ScheduleView()
                .environmentObject(ScheduleViewModel(observationFactory: IOSObservationFactory()))
        } topBarContent: {
            EmptyView()
        }
    }
}
