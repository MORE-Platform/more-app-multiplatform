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

class ScheduleViewModel: ObservableObject {
    let recorder = AppDelegate.shared.dataRecorder
    let scheduleListType: ScheduleListType
    private let coreModel: CoreScheduleViewModel

    let filterViewModel: DashboardFilterViewModel = DashboardFilterViewModel()

    @Published var schedulesByDate: [Date: [ScheduleModel]] = [:]
    @Published var observationErrors: [String: Set<String>] = [:]
    @Published var observationErrorActions: [String: Set<String>] = [:]

    init(scheduleListType: ScheduleListType) {
        self.scheduleListType = scheduleListType
        coreModel = CoreScheduleViewModel(dataRecorder: recorder, scheduleListType: scheduleListType, coreFilterModel: filterViewModel.coreViewModel)
        loadSchedules()

        ViewManager.shared.studyIsUpdatingAsClosure { [weak self] kBool in
            if kBool.boolValue {
                DispatchQueue.main.async {
                    self?.schedulesByDate.removeAll()
                }
            }
        }
    }

    func loadSchedules() {
        coreModel.onScheduleStateUpdated { [weak self] triple in
            print(triple)
            guard let self = self else { return }

            let added = triple.first as? Set<ScheduleModel> ?? []
            let removed = triple.second as? Set<String> ?? []
            let updated = triple.third as? Set<ScheduleModel> ?? []

            let idsToRemove = removed.union(updated.map { $0.scheduleId })

            if !removed.isEmpty || !updated.isEmpty {
                for (date, schedules) in schedulesByDate {
                    let filteredSchedules = schedules.filter { !idsToRemove.contains($0.scheduleId) }
                    if filteredSchedules.isEmpty {
                        DispatchQueue.main.async {
                            self.schedulesByDate.removeValue(forKey: date)
                        }
                    } else {
                        DispatchQueue.main.async {
                            self.schedulesByDate[date] = filteredSchedules
                        }
                    }
                }
            }

            if !added.isEmpty || !updated.isEmpty {
                let itemsToBeAdded = self.mergeSchedules(Array(added), Array(updated))
                let groupedSchedulesToAdd = Dictionary(grouping: itemsToBeAdded, by: { $0.start.startOfDate() })

                for (date, schedules) in groupedSchedulesToAdd {
                    var existingSchedules = self.schedulesByDate[date] ?? []
                    existingSchedules.append(contentsOf: schedules)
                    let uniqueSchedules = self.removeDuplicates(from: existingSchedules)

                    let sortedSchedules = uniqueSchedules.sorted(by: {
                        if $0.start == $1.start {
                            if $0.end == $1.end {
                                if $0.observationTitle == $1.observationTitle {
                                    return $0.scheduleId < $1.scheduleId
                                }
                                return $0.observationTitle < $1.observationTitle
                            }
                            return $0.end < $1.end
                        }
                        return $0.start < $1.start
                    })

                    DispatchQueue.main.async {
                        self.schedulesByDate[date] = sortedSchedules
                    }
                }
            }
        }

        AppDelegate.shared.observationFactory.observationErrorsAsClosure { [weak self] errors in
            DispatchQueue.main.async {
                if let self = self {
                    self.observationErrors = errors.filterValues { $0 != Observation_.companion.ERROR_DEVICE_NOT_CONNECTED }
                    self.observationErrorActions = errors.filterValues { $0 == Observation_.companion.ERROR_DEVICE_NOT_CONNECTED }
                }
            }
        }
    }

    func removeDuplicates(from schedules: [ScheduleModel]) -> [ScheduleModel] {
        var uniqueSchedules = [String: ScheduleModel]()
        for schedule in schedules {
            uniqueSchedules[schedule.scheduleId] = schedule
        }
        return Array(uniqueSchedules.values)
    }

    func viewDidAppear() {
        coreModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreModel.viewDidDisappear()
    }

    func mergeSchedules(_ lhs: [ScheduleModel], _ rhs: [ScheduleModel]) -> [ScheduleModel] {
        let lhsIds = Set(lhs.map { $0.scheduleId })
        let filteredRhs = rhs.filter { !lhsIds.contains($0.scheduleId) }
        return lhs + filteredRhs
    }

    func numberOfObservationErrors() -> Int {
        let errors = Set(observationErrors.values.flatMap { $0 }).count
        return errors > 0 ? errors : Set(observationErrorActions.values.flatMap { $0 }).count
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
