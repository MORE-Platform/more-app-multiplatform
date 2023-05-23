//
//  IOSDataRecorder.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

class IOSDataRecorder: DataRecorder {
    private let observationManager = ObservationManager(observationFactory: AppDelegate.observationFactory)
    
    func start(scheduleId: String) {
        Task { @MainActor in
            do {
                try await observationManager.start(scheduleId: scheduleId)
            } catch {
                print(error)
            }
        }
    }
    
    func pause(scheduleId: String) {
        observationManager.pause(scheduleId: scheduleId)
    }
    
    func stop(scheduleId: String) {
        observationManager.stop(scheduleId: scheduleId)
    }
    
    func stopAll() {
        observationManager.stopAll()
    }
    
    func restartAll() {
        Task { @MainActor in
            do {
                try await observationManager.restartStillRunning()
            } catch {
                print(error)
            }
        }
    }
    
    func updateTaskStates() {
        observationManager.updateTaskStates()
    }
    
    func activateScheduleUpdate() {
        observationManager.activateScheduleUpdate()
    }
}
