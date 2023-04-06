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
        VStack {
            List(viewModel.scheduleDates, id: \.self) { key in
                if let schedules = viewModel.schedules[key.toKotlinLong()] {
                    Section {
                        ScheduleList(viewModel: viewModel, scheduleModels: schedules)
                    } header: {
                        VStack(alignment: .leading) {
                            BasicText(text: .constant(Int64(key).toDateString(dateFormat: "dd.MM.yyyy")), color: Color.more.primaryDark)
                                .font(Font.more.headline)
                            Divider()
                        }
                    }
                    .padding(.bottom)
                    .hideListRowSeparator()
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.more.secondaryLight)
                }
                
            }
            .listStyle(.plain)
            .clearListBackground()
        }

    }
}

struct ScheduleView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            ScheduleView(viewModel: ScheduleViewModel(observationFactory: IOSObservationFactory()))
        } topBarContent: {
            EmptyView()
        }
    }
}
