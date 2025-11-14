//
//  TaskScheduleService.swift
//  More
//
//  Created by Jan Cortiel on 17.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Foundation
import shared

class TaskScheduleService {
    private let scheduleRepository = ScheduleRepository()
    private var timer: Timer?
    
    func startUpdateTimer() {
        timer?.invalidate()
        update()
    }
    
    private func scheduleNextUpdate() {
        Task { @MainActor in
            if let nextSchedule = try await self.scheduleRepository.getNextSchedule()?.int64Value {
                let now = Int64(Date().timeIntervalSince1970)
                if now < nextSchedule {
                    timer = Timer.scheduledTimer(withTimeInterval: TimeInterval(nextSchedule - now), repeats: false, block: { [weak self] timer in
                        if let self {
                            self.update()
                        } else {
                            timer.invalidate()
                        }
                    })
                } else {
                    stopUpdates()
                }
            } else {
                stopUpdates()
            }
        }
    }
    
    private func update() {
        AppDelegate.shared.updateTaskStates()
        self.scheduleNextUpdate()
    }
    
    private func stopUpdates() {
        timer?.invalidate()
        timer = nil
    }
    
    deinit {
        stopUpdates()
    }
}
