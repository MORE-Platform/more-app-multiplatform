//
//  DataUploadBackgroundTask.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import BackgroundTasks
import Foundation

class DataUploadBackgroundTask {
    static let taskID = "io.redlink.more.app.multiplatform.data-upload"
    static func schedule() {
        let request = BGProcessingTaskRequest(identifier: taskID)
        request.earliestBeginDate = Calendar.current.date(byAdding: .minute, value: 15, to: Date())
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false

        do {
            try BGTaskScheduler.shared.submit(request)
            print("Background Task scheduled")
        } catch {
            print("Error requesting for a background task")
        }
    }

    private let uploadDataManager = AppDelegate.dataUploadManager
    private let dataCollector = ObservationDataCollector()
    
    private var currentUploadingTask: Task<(), Never>? = nil

    private func uploadCollectedData(completion: @escaping (Bool) -> Void) {
        print("Uploading Data in background")
        self.uploadDataManager.uploadData { success in
            if success {
                print("Upload success!")
            } else {
                print("Could not upload!")
            }
            completion(success)
        }
    }
    
    private func collectRecordedData(completion: @escaping () -> Void) {
        print("\(Date()): Collecting recorded data...")
        dataCollector.collectData { dataCollected in
            if dataCollected {
                print("\(Date()): Data collected")
            } else {
                print("\(Date()): No data collected")
            }
            completion()
        }
    }
    
    private func close() {
        self.currentUploadingTask?.cancel()
        self.uploadDataManager.close()
        self.dataCollector.close()
    }
}

extension DataUploadBackgroundTask: BackgroundTaskHandler {
    @MainActor
    func handleProcessingTask(task: BGProcessingTask) {
        print("Starting Background Processing Task")
        task.expirationHandler = {
            print("\(Date()): Task will soon expire! Cleaning up...")
            self.close()
            DataUploadBackgroundTask.schedule()
            print("\(Date()): Cleaned up!")
            DispatchQueue.main.async {
                task.setTaskCompleted(success: false)
            }
        }
        collectRecordedData { [weak self] in
            if let self {
                self.uploadCollectedData(completion: { success in
                    self.close()
                    DataUploadBackgroundTask.schedule()
                    DispatchQueue.main.async {
                        print("\(Date()): Task finished with success: \(success)")
                        task.setTaskCompleted(success: success)
                    }
                })
            } else {
                self?.close()
                DataUploadBackgroundTask.schedule()
                task.setTaskCompleted(success: false)
            }
        }
    }

    func handleRefreshTask(task: BGAppRefreshTask) {
        task.setTaskCompleted(success: true)
    }
    
}
