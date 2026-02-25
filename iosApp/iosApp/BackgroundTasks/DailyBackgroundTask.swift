//
//  DailyBackgroundTask.swift
//
//  Created to run a daily background refresh with a placeholder body.
//

import Foundation
import BackgroundTasks
import shared

enum DailyBackgroundTask {
    // IMPORTANT: Add this identifier to Info.plist under BGTaskSchedulerPermittedIdentifiers
    // and ensure it matches your app's bundle identifier conventions if needed.
    static let taskID = AppDelegate.bundleId + ".dailyRefresh"

    static func setupBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: taskID, using: nil) { task in
            guard let refreshTask = task as? BGAppRefreshTask else {
                task.setTaskCompleted(success: false)
                return
            }
            handle(task: refreshTask)
        }
    }

    static func schedule() {
        let request = BGAppRefreshTaskRequest(identifier: taskID)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 24 * 60 * 60)
        do {
            try BGTaskScheduler.shared.submit(request)
            Napier.d("Scheduled DailyBackgroundTask")
        } catch {
            Napier.e("Failed to schedule DailyBackgroundTask: \(error)")
        }
    }

    private static func handle(task: BGAppRefreshTask) {
        schedule()

        let operationQueue = OperationQueue()
        operationQueue.maxConcurrentOperationCount = 1

        var finished = false
        task.expirationHandler = {
            if !finished {
                Napier.w("DailyBackgroundTask expired before completion")
                task.setTaskCompleted(success: false)
            }
        }

        AppDelegate.shared.updateSchedules { error in
            finished = true
            if let error {
                Napier.e("Updating Schedule Tasks failed: \(error)")
                task.setTaskCompleted(success: false)
            } else {
                Napier.i("Updating scheduling tasks was successful!")
                task.setTaskCompleted(success: true)
            }
        }
    }
}
