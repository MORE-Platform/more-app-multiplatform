//
//  ScheduleViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class ScheduleViewModel: ObservableObject {
    let recorder = IOSDataRecorder()
    let filterViewModel: DashboardFilterViewModel
    let scheduleListType: ScheduleListType
    private let coreModel: CoreScheduleViewModel
    
    private var currentFilters: FilterModel? = nil
    private var originalSchedules: [Int64: [ScheduleModel]] = [:]
    @Published var runningSchedules: [Int64: [ScheduleModel]] = [:]
    @Published var completedSchedules: [Int64: [ScheduleModel]] = [:]
    @Published var schedules: [Int64: [ScheduleModel]] = [:] {
        didSet {
            scheduleDates = Array(schedules.keys.sorted())
        }
    }
    
    @Published var scheduleDates: [Int64] = []
    
    init(observationFactory: IOSObservationFactory, dashboardFilterViewModel: DashboardFilterViewModel, scheduleListType: ScheduleListType) {
        self.scheduleListType = scheduleListType
        self.filterViewModel = dashboardFilterViewModel
        self.coreModel = CoreScheduleViewModel(dataRecorder: recorder, scheduleListType: scheduleListType)
        self.loadSchedules()
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
        filterViewModel.setDateFilterValue()
        filterViewModel.setObservationTypeFilters()
        schedules = filterViewModel.coreModel.applyFilter(scheduleModelList: originalSchedules.convertToKotlinLong()).convertToInt64()
    }
    
    func loadSchedules() {
        coreModel.onScheduleModelListChange { [weak self] scheduleMap in
            if let self {
                self.schedules = scheduleMap.reduce([:]) { partialResult, pair -> [Int64: [ScheduleModel]] in
                    var result = partialResult
                    result[Int64(truncating: pair.key)] = pair.value
                    return result
                }
                self.originalSchedules = self.schedules
            }
        }
    }
}

extension Dictionary<KotlinLong, [ScheduleModel]> {
    func convertToInt64() -> [Int64: [ScheduleModel]] {
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
