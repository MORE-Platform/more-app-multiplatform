//
//  BackgroundSync.swift
//  iosApp
//

import Foundation
import UIKit
import shared

/// Requests extended background execution time for a single async operation
/// (e.g. a BLE stop-and-fetch chain) and automatically cancels when the app
/// re-enters the foreground.
///
/// Usage:
///   let sync = BackgroundSync()
///   let finish = sync.begin(taskName: "Polar360HrStopAndFetch")
///   doAsyncWork { finish() }  // call finish() when done
///
/// - `begin(taskName:)` calls `UIApplication.beginBackgroundTask` and registers
///   a `willEnterForegroundNotification` observer that ends the task early if the
///   user returns to the app.
/// - `end()` is idempotent and thread-safe: UIKit calls are always dispatched to
///   the main thread.
final class BackgroundSync {
    private var taskID: UIBackgroundTaskIdentifier = .invalid
    private var observer: NSObjectProtocol?

    // MARK: - Public API

    /// Starts the background task and the foreground cancellation observer.
    /// - Parameter taskName: Descriptive name shown in Instruments / crash logs.
    /// - Returns: A completion closure — call it when the async work finishes.
    func begin(taskName: String) -> () -> Void {
        onMain { [weak self] in
            guard let self, self.taskID == .invalid else { return }
            self.taskID = UIApplication.shared.beginBackgroundTask(withName: taskName) { [weak self] in
                Napier.w("BackgroundSync '\(taskName)': expired by iOS")
                self?.end()
            }
            self.observer = NotificationCenter.default.addObserver(
                forName: UIApplication.willEnterForegroundNotification,
                object: nil,
                queue: .main
            ) { [weak self] _ in
                Napier.i("BackgroundSync '\(taskName)': app will enter foreground — cancelling")
                self?.end()
            }
            Napier.i("BackgroundSync: started '\(taskName)' (id=\(self.taskID.rawValue))")
        }
        return { [weak self] in self?.end() }
    }

    /// Ends the background task and removes the foreground observer.
    /// Safe to call multiple times and from any thread.
    func end() {
        onMainAsync { [weak self] in
            guard let self else { return }
            if let obs = self.observer {
                NotificationCenter.default.removeObserver(obs)
                self.observer = nil
            }
            guard self.taskID != .invalid else { return }
            Napier.i("BackgroundSync: ending task id=\(self.taskID.rawValue)")
            UIApplication.shared.endBackgroundTask(self.taskID)
            self.taskID = .invalid
        }
    }

    deinit { end() }

    // MARK: - Threading Helpers

    /// Executes `block` synchronously on the main thread (no-op sync if already main).
    private func onMain(_ block: @escaping () -> Void) {
        if Thread.isMainThread { block() } else { DispatchQueue.main.sync(execute: block) }
    }

    /// Executes `block` asynchronously on the main thread (direct if already main).
    private func onMainAsync(_ block: @escaping () -> Void) {
        if Thread.isMainThread { block() } else { DispatchQueue.main.async(execute: block) }
    }
}
