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

import SwiftUI
import shared

struct TaskDetailsView: View {
    @StateObject private var viewModel: TaskDetailsViewModel

    @State private var scrollViewContentSize: CGSize = .zero

    @EnvironmentObject var navigationModalState: NavigationModalState

    private let stringTable = "TaskDetail"
    private let scheduleStringTable = "ScheduleListView"
    private let navigationStrings = "Navigation"
    private let errorStrings = "Errors"

    init(scheduleId: String) {
        _viewModel = StateObject(wrappedValue: TaskDetailsViewModel(scheduleId: scheduleId))
    }

    var body: some View {
        MoreMainBackgroundView(contentPadding: 0) {
            VStack {
                VStack {
                    HStack {
                        Title2(titleText: viewModel.taskDetailsModel?.observationTitle ?? "")
                            .padding(0.5)
                        Spacer()
                        if let detailsModel = viewModel.taskDetailsModel, detailsModel.state == .running, let scheduleId = navigationModalState.navigationState(for: .taskDetails)?.scheduleId {
                            InlineAbortButton {
                                viewModel.stop(scheduleId: scheduleId)
                            }
                            .disabled(!viewModel.taskObservationErrors.isEmpty)
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
                    AccordionItem(title: "Participant Information", info: viewModel.taskDetailsModel?.participantInformation ?? "")
                }
                if let detailsModel = viewModel.taskDetailsModel, !detailsModel.state.completed() {
                    Spacer()
                    HStack {
                        DatapointsCollection(datapoints: $viewModel.dataCount, running: detailsModel.state == .running)
                    }
                    Spacer()

                    VStack {
                        ObservationErrorListView(taskObservationErrors: viewModel.taskObservationErrors, taskObservationErrorActions: viewModel.taskObservationErrorAction)
                            .background(
                                GeometryReader { geo in
                                    Color.clear
                                        .onAppear {
                                            DispatchQueue.main.async {
                                                scrollViewContentSize = geo.size
                                            }
                                        }
                                        .onChange(of: geo.size) { newSize in
                                            DispatchQueue.main.async {
                                                scrollViewContentSize = newSize
                                            }
                                        }
                                }
                            )
                            .frame(maxWidth: .infinity, maxHeight: 100)

                        if !detailsModel.hidden {
                            if let scheduleId = navigationModalState.navigationState(for: .taskDetails)?.scheduleId {
                                Divider()
                                ObservationButton(
                                    observationActionDelegate: viewModel,
                                    scheduleId: scheduleId,
                                    observationType: detailsModel.observationType,
                                    state: detailsModel.state,
                                    disabled: !detailsModel.state.active() || !viewModel.taskObservationErrors.isEmpty
                                )
                                .padding(.bottom)
                            }
                        }
                    }
                }
            }
            .customNavigationTitle(with: NavigationScreen.taskDetails.localize())
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
    static let database = DatabaseManagerKt.getRoomDatabase(builder: DatabaseManager_iosKt.getDatabaseBuilder())
    static let repos = MainRepositoryImpl(appDatabase: database)
    static var previews: some View {
        TaskDetailsView(scheduleId: "preview-schedule-id")
            .environmentObject(NavigationModalState(repos: repos))
    }
}
