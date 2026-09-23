//
//  BackgroundSync.swift
//  iosApp
//

import Foundation
import UIKit
import shared


final class BackgroundSync {
    private var taskID: UIBackgroundTaskIdentifier = .invalid
    private var observer: NSObjectProtocol?

    
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
