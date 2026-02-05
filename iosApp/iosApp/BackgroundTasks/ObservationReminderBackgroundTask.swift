import Foundation
import BackgroundTasks
import shared

// Schedules periodic refreshes to update observation reminders by
// calling AppDelegate.shared.observationService.scheduleObservationReminder().
// NOTE: Add the identifier below to Info.plist under BGTaskSchedulerPermittedIdentifiers.
// iOS ultimately decides exact run frequency; we request the minimum practical interval.
enum ObservationReminderBackgroundTask {
    // IMPORTANT: Add this identifier to Info.plist -> BGTaskSchedulerPermittedIdentifiers
    static let taskID = "io.redlink.umm.blendedcare.observation-reminder-refresh"

    // Smallest practical interval you can request for BGAppRefresh (system may delay)
    static let minimumInterval: TimeInterval = 15 * 60

    static func setupBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: taskID, using: nil) { task in
            guard let refreshTask = task as? BGAppRefreshTask else {
                task.setTaskCompleted(success: false)
                return
            }
            handle(task: refreshTask)
        }
    }

    static func schedule(earliestBeginDate: Date? = nil) {
        let request = BGAppRefreshTaskRequest(identifier: taskID)
        request.earliestBeginDate = earliestBeginDate ?? Date(timeIntervalSinceNow: minimumInterval)
        do {
            try BGTaskScheduler.shared.submit(request)
            Napier.i("ObservationReminderBackgroundTask::schedule - scheduled for \(request.earliestBeginDate ?? Date())")
        } catch {
            Napier.e("ObservationReminderBackgroundTask::schedule - failed to schedule: \(error)")
        }
    }

    private static func handle(task: BGAppRefreshTask) {
        // Always reschedule next run
        schedule()

        var finished = false
        task.expirationHandler = {
            if !finished {
                Napier.w("ObservationReminderBackgroundTask expired before completion")
                task.setTaskCompleted(success: false)
            }
        }

        Task { @MainActor in
            do {
                try await AppDelegate.shared.observationService.scheduleObservationReminder()
                finished = true
                Napier.i("ObservationReminderBackgroundTask completed successfully")
                task.setTaskCompleted(success: true)
            } catch {
                finished = true
                Napier.e("ObservationReminderBackgroundTask failed: \(error)")
                task.setTaskCompleted(success: false)
            }
        }
    }
}
