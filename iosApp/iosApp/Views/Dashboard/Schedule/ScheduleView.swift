//
//  File.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct ScheduleView: View {
    @StateObject var viewModel: ScheduleViewModel
    @StateObject var simpleQuestionModalStateVM: SimpleQuestionModalStateViewModel = SimpleQuestionModalStateViewModel(isQuestionOpen: false, isQuestionThankYouOpen: false)
    
    var body: some View {
        ScrollView(.vertical) {
            LazyVStack(alignment: .leading, pinnedViews: .sectionHeaders) {
                ForEach(viewModel.scheduleDates, id: \.self) { key in
                    if let schedules = viewModel.schedules[key] {
                        if !schedules.isEmpty {
                            Section {
                                ScheduleList(viewModel: viewModel, scheduleModels: schedules, scheduleListType: viewModel.scheduleListType).environmentObject(simpleQuestionModalStateVM)
                            } header: {
                                VStack(alignment: .leading) {
                                    BasicText(text: .constant(Int64(key).toDateString(dateFormat: "dd.MM.yyyy")), color: Color.more.primaryDark)
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
            }.background(Color.more.secondaryLight)
        }
        .onAppear {
            viewModel.viewDidAppear()
        }
        .onDisappear {
            viewModel.viewDidDisappear()
        }
    }
}

struct ScheduleView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            ScheduleView(viewModel: ScheduleViewModel(observationFactory: IOSObservationFactory(), scheduleListType: .all))
        } topBarContent: {
            EmptyView()
        }
    }
}
