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
    private var runningSchedules: Set<String> = Set()
    
    func start(scheduleId: String) {
        if !runningSchedules.contains(scheduleId) {
            Task { @MainActor in
                do {
                    if (try await AppDelegate.shared.observationManager.start(scheduleId: scheduleId)).boolValue {
                        runningSchedules.insert(scheduleId)
                    }
                } catch {
                    print(error)
                }
            }
        }
    }
    
    func startMultiple(scheduleIds: Set<String>) {
        scheduleIds.filter{!runningSchedules.contains($0)}.forEach { id in
            Task { @MainActor in
                do {
                    if (try await AppDelegate.shared.observationManager.start(scheduleId: id)).boolValue {
                        runningSchedules.insert(id)
                    }
                } catch {
                    print(error)
                }
            }
        }
    }
    
    func pause(scheduleId: String) {
        AppDelegate.shared.observationManager.pause(scheduleId: scheduleId)
        runningSchedules.remove(scheduleId)
    }
    
    func stop(scheduleId: String) {
        AppDelegate.shared.observationManager.stop(scheduleId: scheduleId)
        runningSchedules.remove(scheduleId)
    }
    
    func stopAll() {
        AppDelegate.shared.observationManager.stopAll()
        runningSchedules.removeAll()
    }
    
    func restartAll() {
        Task { @MainActor in
            do {
                try await AppDelegate.shared.observationManager.restartStillRunning()
            } catch {
                print(error)
            }
        }
    }
    
    func updateTaskStates() {
        AppDelegate.shared.observationManager.updateTaskStates()
    }
    
    func activateScheduleUpdate() {
        AppDelegate.shared.observationManager.activateScheduleUpdate()
    }
}
