//
//  File.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import shared
import SwiftUI

struct ScheduleView: View {
    @StateObject var viewModel: ScheduleViewModel
    var body: some View {
        VStack {
            ScrollViewReader { _ in
                ScrollView(.vertical) {
                    if viewModel.schedulesByDate.isEmpty {
                        if viewModel.scheduleListType == ScheduleListType.running {
                            EmptyListView(text: "No running tasks currently")
                        } else if viewModel.scheduleListType == ScheduleListType.completed {
                            EmptyListView(text: "No tasks completed by now")
                        } else {
                            EmptyListView(text: "No tasks to show")
                        }
                    } else {
                        LazyVStack(alignment: .leading, pinnedViews: .sectionHeaders) {
                            ForEach(viewModel.schedulesByDate.keys.sorted(), id: \.self) { key in
                                let schedules = viewModel.schedulesByDate[key, default: []]
                                if !schedules.isEmpty {
                                    Section {
                                        ForEach(schedules, id: \.scheduleId) { schedule in
                                            VStack {
                                                ScheduleListItem(viewModel: viewModel, scheduleModel: schedule, showButton: viewModel.scheduleListType != .completed)
                                                Divider()
                                            }
                                        }
                                    } header: {
                                        VStack(alignment: .leading) {
                                            BasicText(text: key.formattedString(), color: Color.more.primaryDark)
                                                .font(Font.more.headline)
                                            Divider()
                                        }.background(Color.more.secondaryLight)
                                    }
                                    .padding(.bottom)
                                } else {
                                    EmptyView()
                                }
                            }
                        }
                        .background(Color.more.secondaryLight)
                    }
                }
            }
        }
    }
}

struct ScheduleView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            ScheduleView(viewModel: ScheduleViewModel(scheduleListType: .all))
        }
    }
}
