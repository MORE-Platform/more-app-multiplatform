//
//  ScheduleViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
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
import Combine
import KMPNativeCoroutinesCombine

class ScheduleViewModel: ObservableObject {
    let recorder = AppDelegate.shared.dataRecorder
    let scheduleListType: ScheduleListType
    private let coreModel: CoreScheduleViewModel

    let filterViewModel: DashboardFilterViewModel = DashboardFilterViewModel()

    @Published var schedulesByDate: [Date: [ScheduleModel]] = [:]
    @Published var observationErrors: [String: Set<String>] = [:]
    
    private var cancellables = Set<AnyCancellable>()

    init(scheduleListType: ScheduleListType) {
        self.scheduleListType = scheduleListType
        coreModel = CoreScheduleViewModel(repos: AppDelegate.shared.repositories, dataRecorder: AppDelegate.shared.dataRecorder, scheduleListType: scheduleListType, coreFilterModel: filterViewModel.coreViewModel, observationFactory: AppDelegate.shared.observationFactory)
        
        createPublisher(for: coreModel.schedulesByDate)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] schedules in
                self?.schedulesByDate = schedules.mapKeys { $0.toInt64().toDate() }
            }
            .store(in: &cancellables)
        
        createPublisher(for: coreModel.observationErrors)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] errors in
                self?.observationErrors = errors
            }
            .store(in: &cancellables)
    }

    func numberOfObservationErrors() -> Int {
        Set(observationErrors.values.flatMap { $0 }).count
    }
}

extension ScheduleViewModel: ObservationActionDelegate {
    func start(scheduleId: String) {
        coreModel.start(scheduleId: scheduleId)
    }

    func pause(scheduleId: String) {
        coreModel.pause(scheduleId: scheduleId)
    }

    func stop(scheduleId: String) {
        coreModel.stop(scheduleId: scheduleId)
    }
}

