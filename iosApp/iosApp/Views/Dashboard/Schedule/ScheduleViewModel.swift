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
    private let coreModel: CoreScheduleViewModel
    
    @Published var schedules: [Int64: [ScheduleModel]] = [:] {
        didSet {
            scheduleDates = Array(schedules.keys.sorted())
        }
    }

    @Published var scheduleDates: [Int64] = []

    init(observationFactory: IOSObservationFactory, dashboardFilterViewModel: DashboardFilterViewModel) {
        filterViewModel = dashboardFilterViewModel
        coreModel = CoreScheduleViewModel(dataRecorder: recorder, coreFilterModel: dashboardFilterViewModel.coreModel)
        coreModel.onScheduleModelListChange { [weak self] scheduleMap in
            if let self {
                self.schedules = scheduleMap.reduce([:]) { partialResult, pair -> [Int64: [ScheduleModel]] in
                    var result = partialResult
                    result[Int64(truncating: pair.key)] = pair.value
                    return result
                }
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
