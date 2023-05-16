//
//  ScheduleViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class ScheduleViewModel: ObservableObject {
    let recorder = AppDelegate.recorder
    let scheduleListType: ScheduleListType
    private let coreModel: CoreScheduleViewModel

    let filterViewModel: DashboardFilterViewModel = DashboardFilterViewModel()
    
    private var schedules: [ScheduleModel] = []

    @Published var scheduleDates: [Int64] = []
    
    lazy var taskDetailsVM: TaskDetailsViewModel = {
        TaskDetailsViewModel(dataRecorder: recorder)
    }()
    
    lazy var simpleQuestionVM = SimpleQuestionObservationViewModel()

    init(scheduleListType: ScheduleListType) {
        self.scheduleListType = scheduleListType
        self.coreModel = CoreScheduleViewModel(dataRecorder: recorder, scheduleListType: scheduleListType, coreFilterModel: filterViewModel.coreModel)
        self.loadSchedules()
    }

    func loadSchedules() {
        coreModel.scheduleDateListChange { [weak self] dateSet in
            self?.scheduleDates = dateSet.map{$0.int64Value}.sorted()
        }
        coreModel.onScheduleModelListChange { [weak self] schedules in
            if let self {
                self.schedules = schedules.sorted(by: {$0.start < $1.start })
                self.scheduleDates = Set(self.schedules.map { $0.start }).sorted()
            }
        }
    }

    func viewDidAppear() {
        coreModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreModel.viewDidDisappear()
    }
    
    func filterScheduleByDate(scheduleDate: Int64) -> [ScheduleModel] {
        let date = scheduleDate.dateWithoutTime()
        return schedules.filter{ $0.start.dateWithoutTime() == date}
    }

    func getSimpleQuestionObservationVM(scheduleId: String) -> SimpleQuestionObservationViewModel {
        simpleQuestionVM.setScheduleId(scheduleId: scheduleId)
        return simpleQuestionVM
    }

    func getTaskDetailsVM(scheduleId: String) -> TaskDetailsViewModel {
        taskDetailsVM.setSchedule(scheduleId: scheduleId)
        return taskDetailsVM
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
