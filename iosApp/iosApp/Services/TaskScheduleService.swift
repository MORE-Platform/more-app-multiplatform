//
//  TaskScheduleService.swift
//  More
//
//  Created by Jan Cortiel on 17.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

class TaskScheduleService {
    private let observationFactory = IOSObservationFactory()
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
        self.scheduleRepository.updateTaskStates(observationFactory: self.observationFactory)
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
