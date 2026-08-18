import Foundation
import BackgroundTasks
import shared

// Single shared poll task for every currently activated ManualObserver observation (e.g. Health
// Connect) - see PollingObservationRegistry in shared code, which submits/cancels this task only
// when the set of activated observation types actually changes (never redundantly).
// NOTE: Add the identifier below to Info.plist under BGTaskSchedulerPermittedIdentifiers.
enum PollingBackgroundTask {
    static let taskID = AppDelegate.bundleId + ".observation-polling"

    private static var currentInterval: TimeInterval?

    static func setupBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: taskID, using: nil) { task in
            guard let refreshTask = task as? BGAppRefreshTask else {
                task.setTaskCompleted(success: false)
                return
            }
            handle(task: refreshTask)
        }
    }

    static func schedule(interval: TimeInterval) {
        currentInterval = interval
        let request = BGAppRefreshTaskRequest(identifier: taskID)
        request.earliestBeginDate = Date(timeIntervalSinceNow: interval)
        do {
            try BGTaskScheduler.shared.submit(request)
            Napier.i("PollingBackgroundTask::schedule - scheduled for \(request.earliestBeginDate ?? Date()), interval \(interval)s")
        } catch {
            Napier.e("PollingBackgroundTask::schedule - failed to schedule: \(error)")
        }
    }

    static func cancel() {
        currentInterval = nil
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: taskID)
    }

    private static func handle(task: BGAppRefreshTask) {
        // Resubmitting here is the platform mechanic that keeps a one-shot BGAppRefreshTask firing
        // repeatedly while polling is active - unrelated to (and not in conflict with)
        // PollingObservationRegistry never redundantly resubmitting on activate()/deactivate().
        if let interval = currentInterval {
            schedule(interval: interval)
        }

        var finished = false
        task.expirationHandler = {
            if !finished {
                Napier.w("PollingBackgroundTask expired before completion")
                task.setTaskCompleted(success: false)
            }
        }

        Task { @MainActor in
            do {
                try await AppDelegate.shared.observationFactory.pollActiveObservations()
                finished = true
                Napier.i("PollingBackgroundTask completed successfully")
                task.setTaskCompleted(success: true)
            } catch {
                finished = true
                Napier.e("PollingBackgroundTask failed: \(error)")
                task.setTaskCompleted(success: false)
            }
        }
    }
}
