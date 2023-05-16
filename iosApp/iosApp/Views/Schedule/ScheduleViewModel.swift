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
    
    @Published var schedulesByDate: [Date: [ScheduleModel]] = [:]
    
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
        coreModel.onScheduleStateUpdated { [weak self] triple in
            if let self,
                let added = triple.first as? Set<ScheduleModel>,
                let removed = triple.second as? Set<String>,
                let updated = triple.third as? Set<ScheduleModel> {
                
                let idsToRemove = removed.union(updated.map{$0.scheduleId})
                for (date, schedules) in self.schedulesByDate {
                    self.schedulesByDate[date] = schedules.filter { !idsToRemove.contains($0.scheduleId)}
                }
                
                let schedulestoAdd = added.union(updated)
                
                let groupedSchedulesToAdd = Dictionary(grouping: schedulestoAdd, by: { $0.start.startOfDate() })
                
                for (date, schedules) in groupedSchedulesToAdd {
                    self.schedulesByDate[date] = (self.schedulesByDate[date, default: []] + schedules).sorted(by: { $0.start < $1.start})
                }
                self.schedulesByDate = self.schedulesByDate.filter { !$1.isEmpty }
            }
        }
    }

    func viewDidAppear() {
        coreModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreModel.viewDidDisappear()
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
