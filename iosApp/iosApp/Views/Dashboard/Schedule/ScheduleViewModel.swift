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
    private let coreModel: CoreScheduleViewModel
    private let coreFilterViewModel: CoreDashboardFilterViewModel = CoreDashboardFilterViewModel()
    @Published var currentFilters: FilterModel? = nil
    @Published var schedules: [KotlinLong : [ScheduleModel]] = [:] {
        didSet {
            self.scheduleDates = Array(self.schedules.keys.map { key in
                key.toInt64()
            }).sorted()
        }
    }
    @Published var scheduleDates: [Int64] = []
    
    init(observationFactory: IOSObservationFactory) {
        recorder.updateTaskStates()
        coreModel = CoreScheduleViewModel(dataRecorder: recorder)
        coreModel.onScheduleModelListChange { [weak self] scheduleMap in
            let states = scheduleMap.flatMap({$0.value}).filter{ $0.scheduleState == ScheduleState.active || $0.scheduleState == ScheduleState.running || $0.scheduleState == ScheduleState.paused}
            if self != nil {
                print("New scheduleMap: \(states)")
                let test =  scheduleMap.reduce([:]) { partialResult, pair -> [Int64: [ScheduleModel]] in
                    var result = partialResult
                    result[Int64(truncating: pair.key)] = pair.value
                    return result
                }
            }
        }
        loadCurrentFilters()
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
    
    func applyFilterToAPI() {
        schedules = coreFilterViewModel.applyFilter(scheduleModelList: schedules)
        loadCurrentFilters()
    }
    
    func loadCurrentFilters() {
        coreFilterViewModel.onLoadCurrentFilters { filters in
            self.currentFilters = filters
        }
    }
}
