import Foundation
import shared

/// Bridges the shared `PollingTaskScheduler` contract to `PollingBackgroundTask`'s BGAppRefreshTask.
class IOSPollingTaskScheduler: PollingTaskScheduler {
    func schedule(intervalMillis: Int64) {
        PollingBackgroundTask.schedule(interval: TimeInterval(intervalMillis) / 1000)
    }

    func cancel() {
        PollingBackgroundTask.cancel()
    }
}
