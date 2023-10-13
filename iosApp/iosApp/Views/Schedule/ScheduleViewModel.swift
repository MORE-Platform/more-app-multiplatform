//
//  ScheduleViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class ScheduleViewModel: ObservableObject {
    let recorder = AppDelegate.shared.dataRecorder
    let scheduleListType: ScheduleListType
    private let coreModel: CoreScheduleViewModel

    let filterViewModel: DashboardFilterViewModel = DashboardFilterViewModel()
    
    @Published var schedulesByDate: [Date: [ScheduleModel]] = [:]
    
    lazy var taskDetailsVM: TaskDetailsViewModel = {
        TaskDetailsViewModel(dataRecorder: recorder)
    }()
    
    lazy var simpleQuestionVM = SimpleQuestionObservationViewModel()
    
    lazy var selflearningQuestionVM = SelfLearningMultipleChoiceQuestionViewModel()

    init(scheduleListType: ScheduleListType) {
        self.scheduleListType = scheduleListType
        self.coreModel = CoreScheduleViewModel(dataRecorder: recorder, scheduleListType: scheduleListType, coreFilterModel: filterViewModel.coreViewModel)
        self.loadSchedules()
    }

    func loadSchedules() {
        coreModel.onScheduleStateUpdated { [weak self] triple in
            if let self,
                let added = triple.first as? Set<ScheduleModel>,
                let removed = triple.second as? Set<String>,
                let updated = triple.third as? Set<ScheduleModel> {
                
                if !removed.isEmpty || !updated.isEmpty {
                    let idsToRemove = removed.union(updated.map{$0.scheduleId})
                    let filtered = self.schedulesByDate.filter { (date, list) in
                        list.contains(where: {idsToRemove.contains($0.scheduleId)})
                    }
                    for (date, schedules) in filtered {
                        self.schedulesByDate[date] = schedules.filter { !removed.contains($0.scheduleId)}
                    }
                }
                
                if !added.isEmpty || !updated.isEmpty {
                    let itemsToBeAdded = self.mergeSchedules(Array(added), Array(updated))
                    
                    let groupedSchedulesToAdd = Dictionary(grouping: itemsToBeAdded, by: { $0.start.startOfDate() })
                    
                    for (date, schedules) in groupedSchedulesToAdd {
                        self.schedulesByDate[date] = self.mergeSchedules(schedules, self.schedulesByDate[date, default: []]).sorted(by: {
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
                    }
                }
            }
        }
    }

    func viewDidAppear() {
        coreModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreModel.viewDidDisappear()
    }

    func getSimpleQuestionObservationVM(navigationState: NavigationState) -> SimpleQuestionObservationViewModel {
        simpleQuestionVM.setScheduleId(navigationState: navigationState)
        return simpleQuestionVM
    }
    
    func getSelfLearningMultipleChoiceQuestionObservationVM(scheduleId: String) -> SelfLearningMultipleChoiceQuestionViewModel {
        selflearningQuestionVM.setScheduleId(scheduleId: scheduleId)
        return selflearningQuestionVM
    }

    func getTaskDetailsVM(navigationState: NavigationState) -> TaskDetailsViewModel {
        if let scheduleId = navigationState.scheduleId {
            taskDetailsVM.setSchedule(scheduleId: scheduleId)
        }
        return taskDetailsVM
    }
    
    func mergeSchedules(_ lhs: [ScheduleModel], _ rhs: [ScheduleModel]) -> [ScheduleModel] {
        let lhsIds = Set(lhs.map{$0.scheduleId})
        let filteredRhs = rhs.filter{ !lhsIds.contains($0.scheduleId) }
        return lhs + filteredRhs
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
