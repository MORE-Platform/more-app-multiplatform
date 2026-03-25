//
//  IOSDataRecorder.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
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

class IOSDataRecorder: DataRecorder {
    private var runningSchedules: Set<String> = Set()

    // MARK: - Background schedule ID persistence

    private static let backgroundScheduleIdsKey = "polar360.backgroundScheduleIds"

    /// Reads IDs that were persisted when observations started.
    /// Used by Polar360SyncBackgroundTask to re-associate fetched BLE data
    /// with the correct observations after a cold background launch.
    static func loadScheduleIdsFromBackground() -> Set<String> {
        let ids = AppDelegate.appGroupUserDefaults?.stringArray(forKey: backgroundScheduleIdsKey) ?? []
        Napier.i("IOSDataRecorder: loaded \(ids.count) background schedule IDs: \(ids)")
        return Set(ids)
    }

    /// Removes all persisted IDs — call when the app returns to foreground.
    static func clearBackgroundScheduleIds() {
        AppDelegate.appGroupUserDefaults?.removeObject(forKey: backgroundScheduleIdsKey)
        Napier.i("IOSDataRecorder: cleared background schedule IDs")
    }

    private func persistScheduleId(_ id: String) {
        var ids = AppDelegate.appGroupUserDefaults?.stringArray(forKey: Self.backgroundScheduleIdsKey) ?? []
        guard !ids.contains(id) else { return }
        ids.append(id)
        AppDelegate.appGroupUserDefaults?.set(ids, forKey: Self.backgroundScheduleIdsKey)
        Napier.i("IOSDataRecorder: persisted schedule ID '\(id)' for background")
    }

    private func removePersistedScheduleId(_ id: String) {
        var ids = AppDelegate.appGroupUserDefaults?.stringArray(forKey: Self.backgroundScheduleIdsKey) ?? []
        ids.removeAll { $0 == id }
        AppDelegate.appGroupUserDefaults?.set(ids, forKey: Self.backgroundScheduleIdsKey)
        Napier.i("IOSDataRecorder: removed schedule ID '\(id)' from background persistence")
    }

    // MARK: - DataRecorder

    func start(scheduleId: String) {
        if !runningSchedules.contains(scheduleId) {
            Task { @MainActor in
                do {
                    if (try await AppDelegate.shared.observationManager.start(scheduleId: scheduleId)).boolValue {
                        runningSchedules.insert(scheduleId)
                        // Persist immediately so the ID survives if the app is killed
                        // before this observation completes normally.
                        persistScheduleId(scheduleId)
                    }
                } catch {
                    Napier.e("\(error)")
                }
            }
        }
    }

    func startMultiple(scheduleIds: Set<String>) {
        scheduleIds.filter {
            !runningSchedules.contains($0)
        }
        .forEach { id in
            Task { @MainActor in
                do {
                    if (try await AppDelegate.shared.observationManager.start(scheduleId: id)).boolValue {
                        runningSchedules.insert(id)
                        persistScheduleId(id)
                    }
                } catch {
                    Napier.e("\(error)")
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
        // Only remove the persisted ID if we are completing normally in the foreground.
        // If the app is in the background the ID must stay so Polar360SyncBackgroundTask
        // can re-associate the BLE data it fetches.
        if !Polar360Controller.shared.appIsInBackground {
            removePersistedScheduleId(scheduleId)
        }
    }

    func stopAll() {
        AppDelegate.shared.observationManager.stopAll()
        runningSchedules.removeAll()
        // Intentionally do NOT clear persisted IDs here — stopAll() is called when
        // the app backgrounds, so the IDs are needed by the BGAppRefreshTask.
        // They are cleared when the app returns to foreground (.active in iOSApp).
    }

    func restartAll() {
        Task { @MainActor in
            do {
                try await AppDelegate.shared.observationManager.restartStillRunning()
            } catch {
                Napier.e("\(error)")
            }
        }
    }

    func updateTaskStates() {
        Task {
            do {
                try await AppDelegate.shared.observationManager.updateTaskStates()
            } catch {
                Napier.e("Cannot update task states: \(error)")
            }
        }
    }

    func activateScheduleUpdate() {
        AppDelegate.shared.observationManager.activateScheduleUpdate()
    }
}
