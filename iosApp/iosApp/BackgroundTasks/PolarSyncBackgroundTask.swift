//
//  PolarSyncBackgroundTask.swift
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
enum PolarSyncBackgroundTask {
    static let taskID = AppDelegate.bundleId + ".polar-sync"
    /// Earliest-begin hint passed to iOS — the system may still delay longer.
    static let minimumInterval: TimeInterval = 1 * 60

    // Registration

    static func setupBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: taskID, using: nil) { task in
            guard let refreshTask = task as? BGAppRefreshTask else {
                task.setTaskCompleted(success: false)
                return
            }
            handle(task: refreshTask)
        }
    }

    //Scheduling


    static func schedule(earliestBeginDate: Date? = nil) {
        let request = BGAppRefreshTaskRequest(identifier: taskID)
        request.earliestBeginDate = earliestBeginDate ?? Date(timeIntervalSinceNow: minimumInterval)
        do {
            try BGTaskScheduler.shared.submit(request)
            Napier.i("PolarSyncBackgroundTask: scheduled for \(request.earliestBeginDate!)")
        } catch {
            Napier.e("PolarSyncBackgroundTask: failed to schedule — \(error)")
        }
    }

    // MARK: - Handling

    private static func handle(task: BGAppRefreshTask) {
        schedule()

        PolarController.shared.appIsInBackground = true
        
        
        PolarController.shared.restoreDeviceIdFromBackground()

        
        let scheduleIds = IOSDataRecorder.loadScheduleIdsFromBackground()
        Napier.i("PolarSyncBackgroundTask: handling with \(scheduleIds.count) schedule IDs: \(scheduleIds)")

        var finished = false
        task.expirationHandler = {
            if !finished {
                Napier.w("PolarSyncBackgroundTask: expired before completion")
                PolarController.shared.appIsInBackground = false
                task.setTaskCompleted(success: false)
            }
        }

        ObservationDataCollector().collectData { success in
            finished = true
            PolarController.shared.appIsInBackground = false
            Napier.i("PolarSyncBackgroundTask: completed (success=\(success))")
            task.setTaskCompleted(success: success)
        }
    }
}
