//
//  Polar360SyncBackgroundTask.swift
//  iosApp
//

import BackgroundTasks
import Foundation
import shared

/// BGAppRefreshTask that periodically collects and uploads polar offline observation
/// data while the app is backgrounded.
///
/// - Register: call `setupBackgroundTasks()` from `application(_:didFinishLaunchingWithOptions:)`.
/// - Schedule: call `schedule(earliestBeginDate:)` whenever the app transitions to background.
/// - Cancel:   call `BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: taskID)` on foreground.
enum Polar360SyncBackgroundTask {
    static let taskID = AppDelegate.bundleId + ".polar360-sync"
    /// Earliest-begin hint passed to iOS — the system may still delay longer.
    static let minimumInterval: TimeInterval = 1 * 60

    // MARK: - Registration

    static func setupBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: taskID, using: nil) { task in
            guard let refreshTask = task as? BGAppRefreshTask else {
                task.setTaskCompleted(success: false)
                return
            }
            handle(task: refreshTask)
        }
    }

    // MARK: - Scheduling

    /// Submits a BGAppRefreshTaskRequest.
    /// - Parameter earliestBeginDate: Earliest date iOS may execute the task.
    ///   Pass `nil` to use `minimumInterval` seconds from now.
    static func schedule(earliestBeginDate: Date? = nil) {
        let request = BGAppRefreshTaskRequest(identifier: taskID)
        request.earliestBeginDate = earliestBeginDate ?? Date(timeIntervalSinceNow: minimumInterval)
        do {
            try BGTaskScheduler.shared.submit(request)
            Napier.i("Polar360SyncBackgroundTask: scheduled for \(request.earliestBeginDate!)")
        } catch {
            Napier.e("Polar360SyncBackgroundTask: failed to schedule — \(error)")
        }
    }

    // MARK: - Handling

    private static func handle(task: BGAppRefreshTask) {
        // Re-schedule immediately so the next run is queued even if this one expires early.
        schedule()

        // Mark as background so that stop(scheduleId:) and stopOfflineRecordingAndFetch
        // do not prematurely remove persisted IDs or skip the UserDefaults fallback.
        // This flag is normally set by iOSApp.onChange but is not set on a cold launch.
        Polar360Controller.shared.appIsInBackground = true

        // Restore the device ID that was saved when the observation started so
        // stopOfflineRecordingAndFetch can find the Polar device after a cold launch.
        Polar360Controller.shared.restoreDeviceIdFromBackground()

        // Log the schedule IDs that were persisted when observations started.
        // collectData → updateTaskStates() re-hydrates KMP from its DB, which uses
        // these same IDs to tag the fetched BLE data before it reaches the backend.
        let scheduleIds = IOSDataRecorder.loadScheduleIdsFromBackground()
        Napier.i("Polar360SyncBackgroundTask: handling with \(scheduleIds.count) schedule IDs: \(scheduleIds)")

        var finished = false
        task.expirationHandler = {
            if !finished {
                Napier.w("Polar360SyncBackgroundTask: expired before completion")
                Polar360Controller.shared.appIsInBackground = false
                task.setTaskCompleted(success: false)
            }
        }

        ObservationDataCollector().collectData { success in
            finished = true
            Polar360Controller.shared.appIsInBackground = false
            Napier.i("Polar360SyncBackgroundTask: completed (success=\(success))")
            task.setTaskCompleted(success: success)
        }
    }
}
