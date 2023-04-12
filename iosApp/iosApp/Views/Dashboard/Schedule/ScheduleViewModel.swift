//
//  ScheduleViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class ScheduleViewModel: ObservableObject {
    let recorder = IOSDataRecorder()
    let filterViewModel: DashboardFilterViewModel
    private let coreModel: CoreScheduleViewModel
    private var currentFilters: FilterModel? = nil
    @Published var schedules: [Int64: [ScheduleModel]] = [:] {
        didSet {
            scheduleDates = Array(schedules.keys.sorted())
        }
    }

    @Published var scheduleDates: [Int64] = []

    init(observationFactory: IOSObservationFactory, dashboardFilterViewModel: DashboardFilterViewModel) {
        recorder.updateTaskStates()
        filterViewModel = dashboardFilterViewModel
        coreModel = CoreScheduleViewModel(dataRecorder: recorder)
        loadSchedules()
    }
    
    private func loadSchedules() {
        coreModel.onScheduleModelListChange { [weak self] scheduleMap in
            if let self {
                self.schedules = scheduleMap.converttoInt64()
            }
        }
    }

    func start(scheduleId: String) {
        coreModel.start(scheduleId: scheduleId)
    }

    func pause(scheduleId: String) {
        coreModel.pause(scheduleId: scheduleId)
    }

    func stop(scheduleId: String) {
        coreModel.stop(scheduleId: scheduleId)
    }

    func applyFilters() {
        loadSchedules()
        filterViewModel.setDateFilterValue()
        filterViewModel.setObservationTypeFilters()
        schedules = filterViewModel.coreModel.applyFilter(scheduleModelList: schedules.convertToKotlinLong()).converttoInt64()
    }
}

extension Dictionary<KotlinLong, [ScheduleModel]> {
    func converttoInt64() -> [Int64: [ScheduleModel]] {
        reduce([:]) { partialResult, pair -> [Int64: [ScheduleModel]] in
            var result = partialResult
            result[Int64(truncating: pair.key)] = pair.value
            return result
        }
    }
}

extension Dictionary<Int64, [ScheduleModel]> {
    func convertToKotlinLong() -> [KotlinLong: [ScheduleModel]] {
        reduce([:]) { partialResult, pair -> [KotlinLong: [ScheduleModel]] in
            var result = partialResult
            result[KotlinLong(value: pair.key)] = pair.value
            return result
        }
    }
}
