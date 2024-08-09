//
//  TaskDetailsView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 20.03.23.
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

struct TaskDetailsView: View {
    @StateObject var viewModel: TaskDetailsViewModel

    @State private var scrollViewContentSize: CGSize = .zero

    @EnvironmentObject var navigationModalState: NavigationModalState

    private let stringTable = "TaskDetail"
    private let scheduleStringTable = "ScheduleListView"
    private let navigationStrings = "Navigation"
    private let errorStrings = "Errors"

    var body: some View {
        MoreMainBackgroundView(contentPadding: 0) {
            VStack(spacing: 20) {
                VStack {
                    HStack {
                        Title2(titleText: viewModel.taskDetailsModel?.observationTitle ?? "")
                            .padding(0.5)
                        Spacer()
                        if let detailsModel = viewModel.taskDetailsModel, detailsModel.state == .running, let scheduleId = navigationModalState.navigationState(for: .taskDetails)?.scheduleId {
                            InlineAbortButton {
                                viewModel.stop(scheduleId: scheduleId)
                            }
                        }
                    }
                    .frame(height: 40)

                    HStack {
                        BasicText(text: viewModel.taskDetailsModel?.observationType ?? "", color: .more.secondary)
                        Spacer()
                    }
                }

                ObservationDetailsData(dateRange: viewModel.getDateRangeString(), timeframe: viewModel.getTimeRangeString())

                HStack {
                    AccordionItem(title: String.localize(forKey: "Participant Information", withComment: "Participant Information of specific task.", inTable: stringTable), info: viewModel.taskDetailsModel?.participantInformation ?? "")
                }
                if let detailsModel = viewModel.taskDetailsModel, !detailsModel.state.completed() {
                    Spacer()
                    HStack {
                        DatapointsCollection(datapoints: $viewModel.dataCount, running: detailsModel.state == .running)
                    }
                    Spacer()

                    ObservationErrorListView(taskObservationErrors: viewModel.taskObservationErrors, taskObservationErrorActions: viewModel.taskObservationErrorAction)
                        .background(
                            GeometryReader { geo -> Color in
                                DispatchQueue.main.async {
                                    scrollViewContentSize = geo.size
                                }
                                return Color.clear
                            }
                        )
                        .frame(maxWidth: .infinity, maxHeight: 150)

                    if !detailsModel.hidden {
                        if let scheduleId = navigationModalState.navigationState(for: .taskDetails)?.scheduleId {
                            ObservationButton(
                                observationActionDelegate: viewModel,
                                scheduleId: scheduleId,
                                observationType: detailsModel.observationType,
                                state: detailsModel.state,
                                disabled: !detailsModel.state.active() || !viewModel.taskObservationErrors.isEmpty)
                        }
                    }
                }
                Spacer()
            }
            .customNavigationTitle(with: NavigationScreen.taskDetails.localize(useTable: navigationStrings, withComment: "Task Detail"))
            .onAppear {
                viewModel.viewDidAppear()
            }
            .onDisappear {
                viewModel.viewDidDisappear()
            }
        }
    }
}

struct TaskDetailsViewPreview_Provider: PreviewProvider {
    static var previews: some View {
        TaskDetailsView(viewModel: TaskDetailsViewModel(dataRecorder: AppDelegate.shared.dataRecorder))
            .environmentObject(NavigationModalState())
    }
}
