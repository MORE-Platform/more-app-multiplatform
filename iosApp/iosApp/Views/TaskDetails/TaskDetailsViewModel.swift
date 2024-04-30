//
//  TaskDetailsViewModel.swift
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

import shared
import SwiftUI

class TaskDetailsViewModel: ObservableObject {
    private let coreModel: CoreTaskDetailsViewModel
    
    @Published var taskDetailsModel: TaskDetailsModel? {
        didSet {
            if self.taskDetailsModel == nil {
                self.observationErrors = []
            }
        }
    }
    @Published var dataCount: Int64 = 0
    @Published var observationErrors: [String] = []
    
    var simpleQuestionObservationVM: SimpleQuestionObservationViewModel
    
    private var observationErrorsJob: Ktor_ioCloseable?
    
    init(dataRecorder: DataRecorder) {
        self.coreModel = CoreTaskDetailsViewModel(dataRecorder: dataRecorder)
        self.simpleQuestionObservationVM = SimpleQuestionObservationViewModel()
        coreModel.onLoadTaskDetails { [weak self] taskDetails in
            if let self {
                if let taskDetails {
                    self.taskDetailsModel = taskDetails
                    self.observationErrorsJob = AppDelegate.shared.observationFactory.observationErrorsAsClosure { errors in
                        self.observationErrors = Array(errors[taskDetails.observationType] ?? [])
                    }
                } else {
                    self.observationErrorsJob?.close()
                }
            }
        }
        
        coreModel.onNewDataCount { [weak self] count in
            if let self {
                self.dataCount = count?.int64Value ?? 0
            }
        }
    }
    
    func setSchedule(scheduleId: String) {
        coreModel.setSchedule(scheduleId: scheduleId)
    }
    
    func viewDidAppear() {
        coreModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreModel.viewDidDisappear()
    }

    
    func getDateRangeString() -> String {
        let startDate = taskDetailsModel?.start.toDateString(dateFormat: "dd.MM.yyyy") ?? ""
        let endDate = taskDetailsModel?.end.toDateString(dateFormat: "dd.MM.yyyy") ?? ""
        if(startDate != endDate) {
            return startDate + " - " + endDate
        }
        return startDate
    }
    
    func getTimeRangeString() -> String {
        return (taskDetailsModel?.start.toDateString(dateFormat: "HH:mm") ?? "") + " - " + (taskDetailsModel?.end.toDateString(dateFormat: "HH:mm") ?? "")
    }
}

extension TaskDetailsViewModel: ObservationActionDelegate {
    func start(scheduleId: String) {
        coreModel.startObservation()
    }
    
    func pause(scheduleId: String) {
        coreModel.pauseObservation()
    }
    
    func stop(scheduleId: String) {
        coreModel.stopObservation()
    }
}
